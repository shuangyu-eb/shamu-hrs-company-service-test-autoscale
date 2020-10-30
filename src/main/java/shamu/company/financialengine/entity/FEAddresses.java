package shamu.company.financialengine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.OfficeAddress;

@Data
@Entity
@Table(name = "financial_engine_addresses")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FEAddresses extends BaseEntity {

  @JoinColumn(name = "hris_address_id")
  @OneToOne
  private OfficeAddress officeAddress;

  @Column(name = "fe_address_id")
  private String feAddressId;

  @Enumerated(EnumType.STRING)
  @Column(name = "fe_address_type")
  private FeAddressType type;

  public enum FeAddressType {
    FILING,
    MAILING,
    SATELLITE
  }
}
