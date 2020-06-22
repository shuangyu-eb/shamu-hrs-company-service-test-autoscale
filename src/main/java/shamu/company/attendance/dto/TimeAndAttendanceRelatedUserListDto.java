package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TimeAndAttendanceRelatedUserListDto {
    private List<TimeAndAttendanceRelatedUserDto> selectedUsers;

    private List<TimeAndAttendanceRelatedUserDto> unselectedUsers;
}
