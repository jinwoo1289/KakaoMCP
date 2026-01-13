package com.jinwoo.mcp.departure.controller;

import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.SavePresetRequest;
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

    private final DepartureTimingService departureTimingService;

    /**
     * ✅ MCP 메인 엔드포인트
     * - PlayMCP "정보 불러오기" → initialize
     * - 반드시 JSON-RPC 스펙 응답 필요
     */
    @PostMapping(
            value = {"/mcp", "/mcp/", "/mcp/mcp"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> mcp(
            @RequestBody Map<String, Object> body,
            @RequestHeader Map<String, String> headers
    ) {
        log.info("MCP_REQUEST body={}", body);

        String method = (String) body.get("method");
        Object id = body.get("id");

        // 🔹 initialize (PlayMCP 연결 확인 단계)
        if ("initialize".equals(method)) {
            return ResponseEntity.ok(Map.of(
                    "jsonrpc", "2.0",
                    "id", id,
                    "result", Map.of(
                            "protocolVersion", "2025-06-18",
                            "capabilities", Map.of(
                                    "tools", Map.of()
                            ),
                            "serverInfo", Map.of(
                                    "name", "departure-timing-mcp",
                                    "version", "1.0.0"
                            )
                    )
            ));
        }

        // 🔹 tools/list (확장 대비 – 지금은 빈 목록)
        if ("tools/list".equals(method)) {
            return ResponseEntity.ok(Map.of(
                    "jsonrpc", "2.0",
                    "id", id,
                    "result", Map.of(
                            "tools", new Object[0]
                    )
            ));
        }

        // 🔹 알 수 없는 MCP 메서드
        return ResponseEntity.ok(Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "error", Map.of(
                        "code", -32601,
                        "message", "Method not found"
                )
        ));
    }

    /**
     * ✅ 실제 기능 요청 (REST API 유지)
     * - MCP Tool 내부에서 호출하거나
     * - Postman / 일반 API 호출용
     */
    @PostMapping(
            value = "/mcp/assess",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> assess(@RequestBody AssessDepartureTimingRequest req) {
        return ResponseEntity.ok(departureTimingService.assess(req));
    }

    /**
     * ✅ preset 저장
     */
    @PostMapping(
            value = "/mcp/presets",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> savePreset(@RequestBody SavePresetRequest request) {
        return ResponseEntity.ok(departureTimingService.savePreset(request));
    }
}
