package shamu.company.attendance.dto;

import lombok.Data;
import shamu.company.employee.dto.CompensationDto;

import java.sql.Timestamp;

@Data
public class TimeAndAttendanceRelatedUserDto {
    private String firstName;

    private String preferredName;

    private String id;

    private String imageUrl;

    private String jobTitle;

    private String lastName;

    private String department;

    private String employmentType;

    private Timestamp startDate;

    private CompensationDto compensation;
}
