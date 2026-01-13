package com.jinwoo.mcp.departure.controller;

import com.jinwoo.mcp.departure.DepartureTimingMcpApplication;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.SavePresetRequest;
import com.jinwoo.mcp.departure.dto.SavePresetResponse;
import com.jinwoo.mcp.departure.service.DepartureTimingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp")
public class DepartureTimingController {
    private final DepartureTimingService departureTimingService;
    @PostMapping(value="/mcp", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> assess(@RequestBody(required = false) AssessDepartureTimingRequest req) {

        // 검증 호출 (body 없음)
        if (req == null) {
            return ResponseEntity.ok(Map.of("status","ok","message","MCP server validated"));
        }

        // 검증 호출 (필수값 없음)
        if (req.getStation() == null || req.getLine() == null) {
            return ResponseEntity.ok(Map.of("status","ok","message","MCP server validated"));
        }

        return ResponseEntity.ok(departureTimingService.assess(req));
    }

    @PostMapping("/presets")
    public SavePresetResponse savePreset(@RequestBody SavePresetRequest request) {
        return departureTimingService.savePreset(request);
    }
}
