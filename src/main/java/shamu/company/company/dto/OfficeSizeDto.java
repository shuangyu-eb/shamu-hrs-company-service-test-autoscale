package shamu.company.company.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class OfficeSizeDto extends OfficeDto {

  Integer size;
}
