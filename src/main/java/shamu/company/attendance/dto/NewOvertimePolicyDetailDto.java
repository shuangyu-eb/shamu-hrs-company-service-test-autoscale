package shamu.company.attendance.dto;

import lombok.Data;
import shamu.company.attendance.entity.StaticOvertimeType.OvertimeType;


@Data
public class NewOvertimePolicyDetailDto {
    private Integer startMin;

    private OvertimeType overtimeType;

    private Double overtimeRate;
}
