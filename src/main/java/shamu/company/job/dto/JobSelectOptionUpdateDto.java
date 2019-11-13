package shamu.company.job.dto;

import lombok.Data;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.hashids.HashidsFormat;

@Data
public class JobSelectOptionUpdateDto {

  @HashidsFormat
  private Long id;

  private String oldName;

  private String newName;

  private String updateField;

  private OfficeCreateDto officeCreateDto;
}
