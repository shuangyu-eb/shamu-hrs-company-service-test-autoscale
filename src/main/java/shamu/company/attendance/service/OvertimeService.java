package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.attendance.dto.OvertimeRuleDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.OvertimePolicyMapper;
import shamu.company.attendance.entity.mapper.PolicyDetailMapper;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.repository.PolicyDetailRepository;
import shamu.company.attendance.repository.StaticOvertimeTypeRepository;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OverTimePayFactory;
import shamu.company.company.entity.Company;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** @author mshumaker */
@Service
public class OvertimeService {

  private static final int CONVERT_SECOND_TO_MS = 1000;
  private static final String DEFAULT_OVERTIME_POLICY_NAME = "default_overtime_policy";

  private final GenericHoursService genericHoursService;
  private final OvertimePolicyRepository overtimePolicyRepository;
  private final OvertimePolicyMapper overtimePolicyMapper;
  private final PolicyDetailMapper policyDetailMapper;
  private final PolicyDetailRepository policyDetailRepository;
  private final StaticOvertimeTypeRepository staticOvertimeTypeRepository;

  public OvertimeService(
      final GenericHoursService genericHoursService,
      final OvertimePolicyRepository overtimePolicyRepository,
      final OvertimePolicyMapper overtimePolicyMapper,
      final PolicyDetailMapper policyDetailMapper,
      final PolicyDetailRepository policyDetailRepository,
      final StaticOvertimeTypeRepository staticOvertimeTypeRepository) {
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
    // TODO use this function when frontend code is complete
    //        final Map<String, List<OvertimeRuleDto>> overtimeRules = getOvertimeRules(timeSheet);
    //        final List<OvertimeDetailDto> overtimeDetailDtos =
    //            new OvertimeCalculator().getOvertimePay(localDateEntries, overtimeRules);
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

  public void saveNewOvertimePolicy(
      final OvertimePolicyDto overtimePolicyDto, final String companyId) {
    final List<OvertimePolicyDetailDto> policyDetailDtos = overtimePolicyDto.getPolicyDetails();
    final OvertimePolicy newOvertimePolicy =
        overtimePolicyMapper.convertToOvertimePolicy(
            new OvertimePolicy(), overtimePolicyDto, companyId);
    final List<PolicyDetail> policyDetails =
        policyDetailDtos.stream()
            .map(
                (OvertimePolicyDetailDto overtimePolicyDto1) ->
                    policyDetailMapper.convertToPolicyDetail(overtimePolicyDto1, newOvertimePolicy))
            .collect(Collectors.toList());
    policyDetails.stream()
        .forEach(
            policyDetail ->
                policyDetail.setStaticOvertimeType(
                    staticOvertimeTypeRepository.findByName(
                        policyDetail.getStaticOvertimeType().getName())));
    overtimePolicyRepository.save(newOvertimePolicy);
    policyDetailRepository.saveAll(policyDetails);
  }

  public void createDefaultPolicy(final Company company) {
    final OvertimePolicy overtimePolicy =
        OvertimePolicy.builder()
            .policyName(DEFAULT_OVERTIME_POLICY_NAME)
            .company(company)
            .defaultPolicy(true)
            .build();
    overtimePolicyRepository.save(overtimePolicy);
  }

  public OvertimePolicy findDefaultPolicy() {
    return overtimePolicyRepository.findByDefaultPolicy(true);
  }

  @Transactional
  public void softDeleteOvertimePolicy(final String policyId) {
    overtimePolicyRepository.softDeleteOvertimePolicy(policyId);
  }

  public Map<String, List<OvertimeRuleDto>> getOvertimeRules(final TimeSheet timeSheet) {
    final OvertimePolicy overtimePolicy = timeSheet.getUserCompensation().getOvertimePolicy();
    final List<PolicyDetail> policyDetails =
        policyDetailRepository.findAllByOvertimePolicyId(overtimePolicy.getId());
    final Map<String, List<OvertimeRuleDto>> otRules = new HashMap<>();
    policyDetails.forEach(
        policyDetail -> {
          final OvertimeRuleDto overtimeRuleDto =
              OvertimeRuleDto.builder()
                  .start(policyDetail.getStart())
                  .rate(policyDetail.getRate().doubleValue())
                  .build();
          otRules.putIfAbsent(policyDetail.getStaticOvertimeType().getName(), new ArrayList<>());
          otRules.get(policyDetail.getStaticOvertimeType().getName()).add(overtimeRuleDto);
        });
    for (final Map.Entry<String, List<OvertimeRuleDto>> otRule : otRules.entrySet()) {
      otRule.getValue().sort(TimeEntryUtils.compareByOvertimeStart);
    }
    return otRules;
  }
}
