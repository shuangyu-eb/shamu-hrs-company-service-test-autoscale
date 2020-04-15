package shamu.company.benefit.dto;

public enum BenefitPlanTypeEnum {
  ALL(""),
  ACTIVE("active"),
  EXPIRED("expired"),
  STARTING("starting"),
  OTHER("other");

  private String desc;

  BenefitPlanTypeEnum(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

  public static BenefitPlanTypeEnum getEnumByDesc(String desc) {
    for (BenefitPlanTypeEnum deviceType : BenefitPlanTypeEnum.values()) {
      if (desc.equals(deviceType.desc)) {
        return deviceType;
      }
    }
    return OTHER;
  }

}
