package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.company.dto.OfficeDto;
import shamu.company.user.entity.User;

@Data
public class BasicJobInformationDto {

  private String jobUserId;

  private User.Role userRole;

  private SelectFieldInformationDto job;

  private SelectFieldInformationDto employmentType;

  private Timestamp startDate;

  private Timestamp endDate;

  private SelectFieldInformationDto manager;

  private SelectFieldInformationDto department;

  private OfficeDto office;
}
