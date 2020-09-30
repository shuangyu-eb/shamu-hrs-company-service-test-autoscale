package shamu.company.attendance.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.NewOvertimePolicyDetailDto;
import shamu.company.attendance.dto.NewOvertimePolicyDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.attendance.dto.OvertimePolicyOverviewDto;
import shamu.company.attendance.dto.OvertimeRuleDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.OvertimePolicyMapper;
import shamu.company.attendance.entity.mapper.PolicyDetailMapper;
import shamu.company.attendance.exception.PolicyNameExistException;
import shamu.company.attendance.pojo.OvertimePolicyOverviewPojo;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.repository.PolicyDetailRepository;
import shamu.company.attendance.repository.StaticOvertimeTypeRepository;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OvertimeCalculator;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.exception.NotFoundException;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** @author mshumaker */
@Service
public class OvertimeService {

  private static final int CONVERT_SECOND_TO_MS = 1000;
  private static final int CONVERT_HOUR_TO_MIN = 60;

  private final GenericHoursService genericHoursService;
  private final OvertimePolicyRepository overtimePolicyRepository;
  private final OvertimePolicyMapper overtimePolicyMapper;
  private final PolicyDetailMapper policyDetailMapper;
  private final PolicyDetailRepository policyDetailRepository;
  private final StaticOvertimeTypeRepository staticOvertimeTypeRepository;
  private final UserCompensationService userCompensationService;
  private final JobUserRepository jobUserRepository;

  public OvertimeService(
      final GenericHoursService genericHoursService,
      final OvertimePolicyRepository overtimePolicyRepository,
      final OvertimePolicyMapper overtimePolicyMapper,
      final PolicyDetailMapper policyDetailMapper,
      final PolicyDetailRepository policyDetailRepository,
      final StaticOvertimeTypeRepository staticOvertimeTypeRepository,
      final UserCompensationService userCompensationService,
      final JobUserRepository jobUserRepository) {
    this.genericHoursService = genericHoursService;
    this.overtimePolicyRepository = overtimePolicyRepository;
    this.overtimePolicyMapper = overtimePolicyMapper;
    this.policyDetailMapper = policyDetailMapper;
    this.policyDetailRepository = policyDetailRepository;
    this.staticOvertimeTypeRepository = staticOvertimeTypeRepository;
    this.userCompensationService = userCompensationService;
    this.jobUserRepository = jobUserRepository;
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
    final Map<String, List<OvertimeRuleDto>> overtimeRules = getOvertimeRules(timeSheet);
    final List<OvertimeDetailDto> overtimeDetailDtos =
        new OvertimeCalculator().getOvertimePay(localDateEntries, overtimeRules);
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
                    final long startOfOt =
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

  private OvertimePolicy createNewOvetimePolicy(final NewOvertimePolicyDto newOvertimePolicyDto) {
    final OvertimePolicy newOvertimePolicy =
        overtimePolicyMapper.convertToOvertimePolicy(new OvertimePolicy(), newOvertimePolicyDto);
    return overtimePolicyRepository.save(newOvertimePolicy);
  }

  private void saveNewOvertimeDetails(
      final List<NewOvertimePolicyDetailDto> policyDetailDtos,
      final OvertimePolicy newOvertimePolicy) {
    final List<PolicyDetail> policyDetails =
        policyDetailDtos.stream()
            .map(
                (NewOvertimePolicyDetailDto overtimePolicyDto1) ->
                    policyDetailMapper.convertToPolicyDetail(overtimePolicyDto1, newOvertimePolicy))
            .collect(Collectors.toList());
    policyDetails.forEach(
        policyDetail -> {
          policyDetail.setStaticOvertimeType(
              staticOvertimeTypeRepository.findByName(
                  policyDetail.getStaticOvertimeType().getName()));
          policyDetail.setStart(policyDetail.getStart() * CONVERT_HOUR_TO_MIN);
        });
    policyDetailRepository.saveAll(policyDetails);
  }

  private void unsetOldDefaultPolicies(final String newDefaultPolicy) {
    overtimePolicyRepository.unsetOldDefaultOvertimePolices(newDefaultPolicy);
  }

  private boolean overtimePolicyNameExists(final String name) {
    return overtimePolicyRepository.countByPolicyName(name) > 0;
  }

  @Transactional
  public OvertimePolicy saveNewOvertimePolicy(final NewOvertimePolicyDto newOvertimePolicyDto) {
    if (overtimePolicyNameExists(newOvertimePolicyDto.getPolicyName())) {
      throw new PolicyNameExistException(
          "This Policy Already Exists", "POLICY NAME EXIST EXCEPTION");
    }
    final OvertimePolicy overtimePolicy = createNewOvetimePolicy(newOvertimePolicyDto);
    saveNewOvertimeDetails(newOvertimePolicyDto.getPolicyDetails(), overtimePolicy);
    if (Boolean.TRUE.equals(overtimePolicy.getDefaultPolicy())) {
      unsetOldDefaultPolicies(overtimePolicy.getId());
    }
    return overtimePolicy;
  }

  @Transactional
  public void updateOvertimePolicy(final OvertimePolicyDto overtimePolicyDto) {
    final String oldPolicyId = overtimePolicyDto.getId();
    softDeleteOvertimePolicy(oldPolicyId);

    // this policy should be active
    final OvertimePolicy editedOverPolicy =
        overtimePolicyRepository.save(
            overtimePolicyMapper.convertDtoToNewOvertimePolicy(
                new OvertimePolicy(), overtimePolicyDto));
    if (Boolean.TRUE.equals(editedOverPolicy.getDefaultPolicy())) {
      unsetOldDefaultPolicies(editedOverPolicy.getId());
    }

    userCompensationService.updateByEditOvertimePolicyDetails(oldPolicyId, editedOverPolicy);
    saveNewOvertimeDetails(overtimePolicyDto.getPolicyDetails(), editedOverPolicy);
  }

  public void createNotEligiblePolicy() {
    final OvertimePolicy overtimePolicy =
        OvertimePolicy.builder()
            .policyName(OvertimePolicy.NOT_ELIGIBLE_POLICY_NAME)
            .defaultPolicy(true)
            .active(true)
            .build();
    overtimePolicyRepository.save(overtimePolicy);
  }

  public List<OvertimePolicyOverviewDto> findAllOvertimePolicies() {
    final List<OvertimePolicyOverviewPojo> overtimePolicyOverviewPojoList =
        sortOvertimePolicies(overtimePolicyRepository.findOvertimeOverview());
    final List<OvertimePolicyOverviewDto> overtimePolicyOverviewDtoList = new ArrayList<>();
    overtimePolicyOverviewPojoList.forEach(
        overtimePolicyOverviewPojo -> {
          final OvertimePolicyOverviewDto overtimePolicyOverviewDto =
              new OvertimePolicyOverviewDto();
          BeanUtils.copyProperties(overtimePolicyOverviewPojo, overtimePolicyOverviewDto);
          overtimePolicyOverviewDtoList.add(overtimePolicyOverviewDto);
        });
    return overtimePolicyOverviewDtoList;
  }

  private List<OvertimePolicyOverviewPojo> sortOvertimePolicies(
      final List<OvertimePolicyOverviewPojo> overtimePolicyOverviewPojoList) {
    final List<OvertimePolicyOverviewPojo> sortedList = new ArrayList<>();
    for (final OvertimePolicyOverviewPojo overtimePolicyOverviewPojo :
        overtimePolicyOverviewPojoList) {
      if (OvertimePolicy.NOT_ELIGIBLE_POLICY_NAME.equals(
          overtimePolicyOverviewPojo.getPolicyName())) {
        sortedList.add(overtimePolicyOverviewPojo);
        overtimePolicyOverviewPojoList.remove(overtimePolicyOverviewPojo);
        break;
      }
    }
    sortedList.addAll(overtimePolicyOverviewPojoList);
    return sortedList;
  }

  public OvertimePolicy findDefaultPolicy() {
    return overtimePolicyRepository.findByDefaultPolicyIsTrue();
  }

  public OvertimePolicy findPolicyByName(final String name) {
    return overtimePolicyRepository.findByPolicyName(name);
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

  public OvertimePolicyDto findOvertimePolicyDetails(final String policyId) {
    final Optional<OvertimePolicy> overtimePolicy = overtimePolicyRepository.findById(policyId);
    final List<PolicyDetail> policyDetails =
        policyDetailRepository.findAllByOvertimePolicyId(policyId);
    if (overtimePolicy.isPresent()) {
      final List<OvertimePolicyDetailDto> policyDetailDtos =
          policyDetails.stream()
              .map(
                  policyDetail -> {
                    policyDetail.setStart(policyDetail.getStart() / CONVERT_HOUR_TO_MIN);
                    return policyDetailMapper.convertToOvertimePolicyDetailDto(policyDetail);
                  })
              .collect(Collectors.toList());
      return overtimePolicyMapper.convertToOvertimePolicyDto(
          overtimePolicy.get(), policyDetailDtos);
    } else {
      throw new NotFoundException("OVERTIME POLICY NOT FOUND", "OT POLICY EXCEPTION");
    }
  }

  public List<String> findAllPolicyNames() {
    return overtimePolicyRepository.findAllPolicyNames();
  }

  public List<UserCompensation> updateEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> employeeOvertimeDetailsDtoList) {
    saveHireDates(employeeOvertimeDetailsDtoList);
    return userCompensationService.updateByEditEmployeeOvertimePolicies(
        employeeOvertimeDetailsDtoList);
  }

  public List<UserCompensation> createEmployeeOvertimePolicies(
      final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList, final Date startDate) {
    saveHireDates(overtimeDetailsDtoList);
      return userCompensationService.updateByCreateEmployeeOvertimePolicies(
        overtimeDetailsDtoList, startDate);
  }

  private void saveHireDates(final List<EmployeeOvertimeDetailsDto> overtimeDetailsDtoList) {
    final List<JobUser> jobUsers =
        overtimeDetailsDtoList.stream()
            .map(
                employeeOvertimeDetailsDto -> {
                  JobUser jobUser =
                      jobUserRepository.findByUserId(employeeOvertimeDetailsDto.getEmployeeId());
                  final Timestamp hireDate =
                      new Timestamp(employeeOvertimeDetailsDto.getHireDate().getTime());
                  if (jobUser == null) {
                    jobUser = new JobUser();
                  }
                  jobUser.setStartDate(hireDate);
                  return jobUser;
                })
            .collect(Collectors.toList());
    jobUserRepository.saveAll(jobUsers);
  }
}
