package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class OvertimePolicyOverviewDto {
    private String id;

    private String policyName;

    private Boolean defaultPolicy;

    private Integer numberOfEmployees;

}
