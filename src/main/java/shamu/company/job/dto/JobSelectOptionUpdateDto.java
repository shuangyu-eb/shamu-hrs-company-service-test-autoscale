package shamu.company.job.dto;

import lombok.Data;
import shamu.company.company.dto.OfficeCreateDto;

@Data
public class JobSelectOptionUpdateDto {

  private String id;

  private String oldName;

  private String newName;

  private JobSelectOptionUpdateField updateField;

  private OfficeCreateDto officeCreateDto;
}
