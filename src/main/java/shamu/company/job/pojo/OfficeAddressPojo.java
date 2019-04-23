package shamu.company.job.pojo;

import lombok.Data;

@Data
public class OfficeAddressPojo {

  private String officeName;

  private String street1;

  private String street2;

  private String city;

  private Long state;

  private String zip;

  private Long userId;
}
