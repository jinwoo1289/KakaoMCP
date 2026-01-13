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
                                    // 기존 assess
                                    Map.of(
                                            "name", "assess_departure_timing",
                                            "description",
                                            "지하철 실시간 도착 정보를 바탕으로 최적 출발 시점을 판단합니다.",
                                            "inputSchema", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "station", Map.of(
                                                                    "type", "string",
                                                                    "description", "출발하려는 지하철역 이름 (예: 서울역)"
                                                            ),
                                                            "line", Map.of(
                                                                    "type", "string",
                                                                    "description", "지하철 노선 (예: 1호선)"
                                                            ),
                                                            "estimatedTimeToStation", Map.of(
                                                                    "type", "number",
                                                                    "description", "집에서 역까지 이동 시간(분)"
                                                            ),
                                                            "presetName", Map.of(
                                                                    "type", "string",
                                                                    "description", "저장된 이동 시간 프리셋 이름"
                                                            )

                                                    ),
                                                    "required", new String[]{"station", "line"}
                                            )
                                    ),

                                    // ✅ save preset 추가
                                    Map.of(
                                            "name", "save_preset",
                                            "description",
                                            "집에서 역까지의 이동 시간을 프리셋으로 저장합니다.",
                                            "inputSchema", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "presetName", Map.of(
                                                                    "type", "string",
                                                                    "description", "프리셋 이름"
                                                            ),
                                                            "estimatedTimeToStation", Map.of(
                                                                    "type", "number",
                                                                    "description", "집에서 역까지 이동 시간(분)"
                                                            )
                                                    ),
                                                    "required", new String[]{
                                                            "presetName",
                                                            "estimatedTimeToStation"
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

            // 🔹 assess_departure_timing
            if ("assess_departure_timing".equals(toolName)) {
                AssessDepartureTimingRequest req = new AssessDepartureTimingRequest();
                req.setStation((String) arguments.get("station"));
                req.setLine((String) arguments.get("line"));

                if (arguments.get("estimatedTimeToStation") != null) {
                    req.setEstimatedTimeToStation(
                            ((Number) arguments.get("estimatedTimeToStation")).intValue()
                    );
                }

                if (arguments.get("presetName") != null) {
                    req.setPresetName((String) arguments.get("presetName"));
                }

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

            // 🔹 save_preset
            if ("save_preset".equals(toolName)) {
                SavePresetRequest req = new SavePresetRequest();
                req.setPresetName((String) arguments.get("presetName"));
                req.setEstimatedTimeToStation(
                        ((Number) arguments.get("estimatedTimeToStation")).intValue()
                );

                Object result = departureTimingService.savePreset(req);

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
