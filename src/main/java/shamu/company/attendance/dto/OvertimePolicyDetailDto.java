package shamu.company.attendance.dto;

import lombok.Data;
import shamu.company.attendance.entity.StaticOvertimeType.OvertimeType;

/**
 * @author mshumaker
 */
@Data
public class OvertimePolicyDetailDto {
    private Integer startMin;

    private OvertimeType overtimeType;

    private Double overtimeRate;
}
