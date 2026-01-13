package com.jinwoo.mcp.departure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.SavePresetRequest;
import com.jinwoo.mcp.departure.dto.SavePresetResponse;
import com.jinwoo.mcp.departure.service.DepartureTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp")
@Slf4j
public class DepartureTimingController {

    private final DepartureTimingService departureTimingService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // 주입 X, 직접 생성

    @PostMapping(consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> assess(@RequestBody(required = false) String rawBody) {

        if (rawBody == null || rawBody.isBlank()) {
            return ResponseEntity.ok(Map.of("status", "ok", "message", "MCP server validated"));
        }

        try {
            Map<String, Object> body = objectMapper.readValue(rawBody, Map.class);

            Object station = body.get("station");
            Object line = body.get("line");

            if (station == null || line == null) {
                return ResponseEntity.ok(Map.of("status", "ok", "message", "MCP server validated"));
            }

            AssessDepartureTimingRequest req =
                    objectMapper.convertValue(body, AssessDepartureTimingRequest.class);

            return ResponseEntity.ok(departureTimingService.assess(req));

        } catch (Exception e) {
            log.warn("MCP validate/parse failed. rawBody={}", rawBody, e);
            return ResponseEntity.ok(Map.of("status", "ok", "message", "MCP server validated"));
        }
    }

    @PostMapping("/presets")
    public SavePresetResponse savePreset(@RequestBody SavePresetRequest request) {
        return departureTimingService.savePreset(request);
    }
}

