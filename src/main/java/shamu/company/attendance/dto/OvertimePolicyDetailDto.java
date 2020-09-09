package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.attendance.entity.StaticOvertimeType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimePolicyDetailDto {
    private String id;

    private Integer startMin;

    private StaticOvertimeType.OvertimeType overtimeType;

    private Double overtimeRate;
}
