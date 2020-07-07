package shamu.company.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OverTimeMinutesDto {

    private String timeLogId;

    private LocalDateTime startTime;

    private String type;

    private double rate;

    private int minutes;
}
