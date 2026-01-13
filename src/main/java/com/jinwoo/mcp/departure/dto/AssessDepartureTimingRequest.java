package com.jinwoo.mcp.departure.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class AssessDepartureTimingRequest {
    private String presetName;
    private Integer estimatedTimeToStation;
    private String station;
    private String line;
    private String direction;
    private LocalTime currentTime;
}
