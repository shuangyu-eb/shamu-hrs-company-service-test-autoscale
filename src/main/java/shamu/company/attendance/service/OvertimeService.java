package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.*;
import shamu.company.attendance.entity.*;
import shamu.company.attendance.entity.mapper.OvertimePolicyMapper;
import shamu.company.attendance.entity.mapper.PolicyDetailMapper;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.repository.PolicyDetailRepository;
import shamu.company.attendance.repository.StaticOvertimeTypeRepository;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OverTimePayFactory;
import shamu.company.utils.DateUtil;

/** @author mshumaker */
@Service
public class OvertimeService {

  private static final int CONVERT_SECOND_TO_MS = 1000;

  private final GenericHoursService genericHoursService;
  private final OvertimePolicyRepository overtimePolicyRepository;
  private final OvertimePolicyMapper overtimePolicyMapper;
  private final PolicyDetailMapper policyDetailMapper;
  private final PolicyDetailRepository policyDetailRepository;
  private final StaticOvertimeTypeRepository staticOvertimeTypeRepository;

  public OvertimeService(final GenericHoursService genericHoursService,
                         final OvertimePolicyRepository overtimePolicyRepository,
                         final OvertimePolicyMapper overtimePolicyMapper,
                         final PolicyDetailMapper policyDetailMapper, PolicyDetailRepository policyDetailRepository, StaticOvertimeTypeRepository staticOvertimeTypeRepository) {
    this.genericHoursService = genericHoursService;
    this.overtimePolicyRepository = overtimePolicyRepository;
    this.overtimePolicyMapper = overtimePolicyMapper;
    this.policyDetailMapper = policyDetailMapper;
    this.policyDetailRepository = policyDetailRepository;
    this.staticOvertimeTypeRepository = staticOvertimeTypeRepository;
  }

  public List<LocalDateEntryDto> getLocalDateEntries(
      final TimeSheet timeSheet, final CompanyTaSetting companyTaSetting) {
    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    // start of 7th consecutive day
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final String userId = timeSheet.getEmployee().getId();
    // get hours from start of week through end of period
    final List<EmployeeTimeLog> allEmployeeEntries =
        genericHoursService.findEntriesBetweenDates(
            startOfTimesheetWeek * CONVERT_SECOND_TO_MS, timesheetEnd.getTime(), userId, true);
    // calculate the OT hours based off company's timezone
    return TimeEntryUtils.transformTimeLogsToLocalDate(
        allEmployeeEntries, companyTaSetting.getTimeZone());
  }

  public List<OvertimeDetailDto> getOvertimeEntries(
      final List<LocalDateEntryDto> localDateEntries,
      final TimeSheet timeSheet,
      final CompanyTaSetting companyTaSetting) {

    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final List<OvertimeDetailDto> overtimeDetailDtos =
        OverTimePayFactory.getOverTimePay(timeSheet.getUserCompensation())
            .getOvertimePay(localDateEntries);
    return filterOvertimeEntries(
        overtimeDetailDtos,
        timeSheetStart.getTime(),
        timesheetEnd.getTime(),
        companyTaSetting.getTimeZone());
  }

  private List<OvertimeDetailDto> filterOvertimeEntries(
      final List<OvertimeDetailDto> overtimeDetails,
      final long start,
      final long end,
      final StaticTimezone timezone) {
    for (final OvertimeDetailDto overtimeDetailDto : overtimeDetails) {
      final ArrayList<OverTimeMinutesDto> otMinDtos =
          overtimeDetailDto.getOverTimeMinutesDtos().stream()
              .filter(
                  overTimeMinutesDto -> {
                    long startOfOt =
                        overTimeMinutesDto
                            .getStartTime()
                            .atZone(ZoneId.of(timezone.getName()))
                            .toInstant()
                            .toEpochMilli();
                    return (startOfOt > start && startOfOt < end);
                  })
              .collect(Collectors.toCollection(ArrayList::new));
      final int otMin =
          otMinDtos.isEmpty()
              ? 0
              : otMinDtos.stream().mapToInt(OverTimeMinutesDto::getMinutes).sum();
      overtimeDetailDto.setTotalMinutes(otMin);
      overtimeDetailDto.setOverTimeMinutesDtos(otMinDtos);
    }
    return overtimeDetails.stream()
        .filter(overtimeDetailDto -> overtimeDetailDto.getTotalMinutes() > 0)
        .collect(Collectors.toList());
  }

  public Map<Double, Integer> findAllOvertimeHours(
      final List<EmployeeTimeLog> workedHours,
      final TimeSheet timeSheet,
      final CompanyTaSetting companyTaSetting) {
    final List<LocalDateEntryDto> localDateEntryDtos =
        TimeEntryUtils.transformTimeLogsToLocalDate(workedHours, companyTaSetting.getTimeZone());
    final List<OvertimeDetailDto> overtimeDetailDtos =
        getOvertimeEntries(localDateEntryDtos, timeSheet, companyTaSetting);
    final Map<Double, Integer> rateToMin = new HashMap<>();
    overtimeDetailDtos.forEach(
        overtimeDetailDto -> {
          final ArrayList<OverTimeMinutesDto> overTimeMinutesDtos =
              overtimeDetailDto.getOverTimeMinutesDtos();
          overTimeMinutesDtos.forEach(
              overTimeMinutesDto -> {
                rateToMin.putIfAbsent(overTimeMinutesDto.getRate(), 0);
                rateToMin.compute(
                    overTimeMinutesDto.getRate(),
                    (key, val) -> val + overTimeMinutesDto.getMinutes());
              });
        });
    return rateToMin;
  }

  public void saveNewOvertimePolicy(final OvertimePolicyDto overtimePolicyDto, final String companyId){
    final List<OvertimePolicyDetailDto> policyDetailDtos = overtimePolicyDto.getPolicyDetails();
    final OvertimePolicy newOvertimePolicy = overtimePolicyMapper.
            convertToOvertimePolicy(new OvertimePolicy(), overtimePolicyDto,companyId);
    final List<PolicyDetail> policyDetails = policyDetailDtos.stream()
            .map((OvertimePolicyDetailDto overtimePolicyDto1) ->
                    policyDetailMapper.convertToPolicyDetail(overtimePolicyDto1,newOvertimePolicy))
            .collect(Collectors.toList());
    policyDetails.stream().forEach(policyDetail -> policyDetail.setStaticOvertimeType(
            staticOvertimeTypeRepository.findByName(policyDetail.getStaticOvertimeType().getName())));
    overtimePolicyRepository.save(newOvertimePolicy);
    policyDetailRepository.saveAll(policyDetails);
  }


  @Transactional
  public void softDeleteOvertimePolicy(final String policyId){
    overtimePolicyRepository.softDeleteOvertimePolicy(policyId);
  }

}
