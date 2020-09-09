package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimePolicyDto {
    private String id;

    private String policyName;

    private Boolean defaultPolicy;

    private List<OvertimePolicyDetailDto> policyDetails;

    private Boolean active;
}
