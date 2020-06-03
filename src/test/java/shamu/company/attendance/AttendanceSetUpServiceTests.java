package shamu.company.attendance;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.service.AttendanceSetUpService;

public class AttendanceSetUpServiceTests {

    @InjectMocks
    AttendanceSetUpService attendanceSetUpService;

    @Mock private CompanyTaSettingRepository companyTaSettingRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findIsAttendanceSetUp() {
        Mockito.when(companyTaSettingRepository.existsByCompanyId("1")).thenReturn(true);
        attendanceSetUpService.findIsAttendanceSetUp("1");
        assertThatCode(
                () ->
                        attendanceSetUpService.findIsAttendanceSetUp("1"))
                .doesNotThrowAnyException();
    }
}
