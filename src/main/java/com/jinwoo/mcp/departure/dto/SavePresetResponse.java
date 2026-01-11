package com.jinwoo.mcp.departure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SavePresetResponse {
    private boolean success;
    private String message;
}
