package shamu.company.company.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Office;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class OfficeDto {

  @HashidsFormat
  private Long id;

  private String name;

  private OfficeAddressDto officeAddress;

  public OfficeDto(Office office) {
    this.id = office.getId();
    this.name = office.getName();
    this.officeAddress = new OfficeAddressDto(office.getOfficeAddress());
  }
}
