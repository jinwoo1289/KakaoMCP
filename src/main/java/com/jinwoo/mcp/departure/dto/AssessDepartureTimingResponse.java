package com.jinwoo.mcp.departure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssessDepartureTimingResponse {
    private Decision decision;
    private String recommendedDepartureTime;
    private String reason;

}


