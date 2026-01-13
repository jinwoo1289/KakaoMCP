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

        if ("tools/list".equals(method)) {
            return ResponseEntity.ok(Map.of(
                    "jsonrpc", "2.0",
                    "id", id,
                    "result", Map.of(
                            "tools", new Object[]{
                                    Map.of(
                                            "name", "assess_departure_timing",
                                            "description",
                                            "지하철 실시간 도착 정보를 바탕으로 역 대기를 최소화할 수 있는 최적 출발 시점을 판단합니다.",
                                            "inputSchema", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "station", Map.of(
                                                                    "type", "string",
                                                                    "description", "출발역 이름"
                                                            ),
                                                            "line", Map.of(
                                                                    "type", "string",
                                                                    "description", "지하철 노선"
                                                            ),
                                                            "direction", Map.of(
                                                                    "type", "string",
                                                                    "description", "상행/하행"
                                                            ),
                                                            "estimatedTimeToStation", Map.of(
                                                                    "type", "number",
                                                                    "description", "집에서 역까지 이동 시간(분)"
                                                            ),
                                                            "presetName", Map.of(
                                                                    "type", "string",
                                                                    "description", "저장된 프리셋 이름 (선택)"
                                                            )
                                                    ),
                                                    "required", new String[]{
                                                            "station",
                                                            "line"
                                                    }
                                            )
                                    )
                            }
                    )
            ));
        }

        if ("tools/call".equals(method)) {
            Map<String, Object> params = (Map<String, Object>) body.get("params");
            String toolName = (String) params.get("name");
            Map<String, Object> arguments =
                    (Map<String, Object>) params.get("arguments");

            if ("assess_departure_timing".equals(toolName)) {
                AssessDepartureTimingRequest req = new AssessDepartureTimingRequest();
                req.setStation((String) arguments.get("station"));
                req.setLine((String) arguments.get("line"));
                req.setEstimatedTimeToStation(
                        ((Number) arguments.get("EstimatedTimeToStation")).intValue()
                );

                Object result = departureTimingService.assess(req);

                return ResponseEntity.ok(Map.of(
                        "jsonrpc", "2.0",
                        "id", id,
                        "result", Map.of(
                                "content", new Object[]{
                                        Map.of(
                                                "type", "text",
                                                "text", result.toString()
                                        )
                                }
                        )
                ));
            }
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
