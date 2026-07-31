package com.jinwoo.mcp.departure.controller;

import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.SavePresetRequest;
import com.jinwoo.mcp.departure.mcp.McpRequestDispatcher;
import com.jinwoo.mcp.departure.service.DepartureTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DepartureTimingController {

    private final McpRequestDispatcher mcpRequestDispatcher; // 생성자 주입 필드에 추가 (RequiredArgsConstructor라 자동 주입됨)

    @PostMapping(
            value = {"/mcp", "/mcp/", "/mcp/mcp"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> mcp(@RequestBody Map<String, Object> body) {
        log.info("MCP_REQUEST body={}", body);
        return ResponseEntity.ok(mcpRequestDispatcher.dispatch(body));
    }
}
