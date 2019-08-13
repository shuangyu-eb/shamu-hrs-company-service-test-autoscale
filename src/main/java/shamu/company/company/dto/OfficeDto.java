package shamu.company.company.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Office;
import shamu.company.hashids.HashidsFormat;

@Data
public class OfficeDto {

  @HashidsFormat
  private Long id;

  private String name;

  private OfficeAddressDto officeAddress;
}
