package com.jinwoo.mcp.departure.dto;

import lombok.Data;

@Data
public class SavePresetRequest {
    private String presetName;
    private Integer estimatedTimeToStation;
}
