package shamu.company.company.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class OfficeDto {

  @HashidsFormat
  private Long id;

  private String name;

  private OfficeAddress officeAddress;

  public OfficeDto(Office office) {
    this.id = office.getId();
    this.name = office.getName();
    this.officeAddress = office.getOfficeAddress();
  }
}
