package shamu.company.timeoff;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestDateService;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;

public class TimeOffRequestServiceTests {

  @InjectMocks
  private TimeOffRequestService timeOffRequestService;

  @Mock
  private TimeOffRequestRepository timeOffRequestRepository;

  @Mock
  private TimeOffPolicyUserService timeOffPolicyUserService;

  @Mock
  private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TimeOffRequestEmailService timeOffRequestEmailService;

  @Mock
  private TimeOffRequestMapper timeOffRequestMapper;

  @Mock
  private TimeOffPolicyService timeOffPolicyService;

  @Mock
  private TimeOffDetailService timeOffDetailService;

  @Mock
  private Auth0Helper auth0Helper;

  @Mock
  private UserService userService;

  @Mock
  private TimeOffRequestApprovalStatusRepository requestApprovalStatusRepository;

  @Mock
  private TimeOffRequestDateService timeOffRequestDateService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class getRequestsByUserAndStatus {

    private User user;

    private User manager;

    private TimeOffApprovalStatus[] statuses;

    private UserRole userRole;


    @BeforeEach
    void setUp() {
      user = new User();
      user.setId("1");
      manager = new User();
      manager.setId("2");
      userRole= new UserRole();
      statuses= new TimeOffApprovalStatus[1];
      statuses[0] = TimeOffApprovalStatus.APPROVED;
    }

    @Test
    void whenUserHasNoManager_thenFindAdminTeamRequests() {
      user.setManagerUser(null);
      timeOffRequestService.getRequestsByUserAndStatus(user,statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findAdminTeamRequests(Mockito.any(),Mockito.any());
    }

    @Test
    void whenUserRoleIsManagerOrAdmin_thenFindManagerTeamRequests() {
      user.setManagerUser(manager);
      userRole.setName(String.valueOf(Role.MANAGER));
      userRole.setId("1");
      user.setUserRole(userRole);
      Mockito.when(auth0Helper.getUserRole(Mockito.anyString()))
          .thenReturn(Role.MANAGER);
      timeOffRequestService.getRequestsByUserAndStatus(user,statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findManagerTeamRequests(Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    void whenUserRoleNotManagerAndNotAdmin_thenFindEmployeeTeamRequests() {
      user.setManagerUser(manager);
      userRole.setName(String.valueOf(Role.MANAGER));
      userRole.setId("1");
      user.setUserRole(userRole);
      Mockito.when(auth0Helper.getUserRole(Mockito.anyString()))
          .thenReturn(Role.MANAGER);
      timeOffRequestService.getRequestsByUserAndStatus(user,statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findEmployeeSelfPendingRequests(Mockito.any(),Mockito.any());
    }
  }

  @Nested
  class initMyTimeOff {

    private Boolean filteredByEndDay;

    private Page<TimeOffRequest> pageRequest;

    private String[] statuses;

    @BeforeEach
    void setUp() {
      filteredByEndDay = false;
      TimeOffRequest timeOffRequest = new TimeOffRequest();
      List<TimeOffRequest> timeOffRequests = new LinkedList<>();
      timeOffRequests.add(timeOffRequest);
      timeOffRequest.setId("1");
      pageRequest = new PageImpl(timeOffRequests);
      statuses = new String[1];
      Mockito.when(timeOffPolicyUserService.existsByUserId(Mockito.anyString())).thenReturn(true);
      Mockito.when(timeOffRequestRepository
          .findByRequesterUserIdFilteredByStartDay(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
          .thenReturn(pageRequest);
      Mockito.when(timeOffRequestRepository
          .findByRequesterUserIdFilteredByStartAndEndDay(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
          .thenReturn(pageRequest);
    }

    @Test
    void whenFilteredByEndDayIsFalse_thenFindByRequesterUserIdFilteredByStartAndEndDay()
        throws Exception {
      PageRequest pageRequest = PageRequest.of(0,1);
      Whitebox.invokeMethod(timeOffRequestService,"getTimeOffDtos",
          "1",null,null, filteredByEndDay,
          statuses, pageRequest);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findByRequesterUserIdFilteredByStartDay(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    void whenFilteredByEndDayIsTrue_thenFindByRequesterUserIdFilteredByStartAndEndDay()
        throws Exception {
      filteredByEndDay = true;
      PageRequest pageRequest = PageRequest.of(0,1);
      Whitebox.invokeMethod(timeOffRequestService,"getTimeOffDtos",
          "1",null,null, filteredByEndDay,
          statuses, pageRequest);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findByRequesterUserIdFilteredByStartAndEndDay(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
    }

  }

}
