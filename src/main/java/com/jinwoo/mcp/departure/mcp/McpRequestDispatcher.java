package com.jinwoo.mcp.departure.mcp;

import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.SavePresetRequest;
import com.jinwoo.mcp.departure.exception.McpInvalidParamsException;
import com.jinwoo.mcp.departure.service.DepartureTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-RPC 2.0 / MCP 프로토콜 처리를 담당하는 디스패처.
 * - Controller는 HTTP 계층만 담당하고, method 라우팅과 envelope 조립은 여기서 처리한다.
 * - 실제 판단 로직(assess/savePreset)은 DepartureTimingService에 위임한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class McpRequestDispatcher {

    private final DepartureTimingService departureTimingService;

    public Map<String, Object> dispatch(Map<String, Object> body) {
        Object id = body.get("id");
        String method = (String) body.get("method");

        try {
            return switch (method == null ? "" : method) {
                case "initialize" -> success(id, initializeResult());
                case "tools/list" -> success(id, toolsListResult());
                case "tools/call" -> success(id, callTool(body));
                default -> error(id, -32601, "Method not found: " + method);
            };
        } catch (McpInvalidParamsException e) {
            log.warn("MCP invalid params: {}", e.getMessage());
            return error(id, -32602, e.getMessage());
        } catch (Exception e) {
            log.error("MCP internal error", e);
            return error(id, -32603, "Internal error: " + e.getMessage());
        }
    }

    private Map<String, Object> initializeResult() {
        return Map.of(
                "protocolVersion", "2025-06-18",
                "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of(
                        "name", "departure-timing-mcp",
                        "version", "1.0.0"
                )
        );
    }

    private Map<String, Object> toolsListResult() {
        return Map.of(
                "tools", new Object[]{
                        Map.of(
                                "name", "assess_departure_timing",
                                "description", "지하철 실시간 도착 정보를 바탕으로 최적 출발 시점을 판단합니다.",
                                "inputSchema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "station", Map.of("type", "string", "description", "출발하려는 지하철역 이름 (예: 서울역)"),
                                                "line", Map.of("type", "string", "description", "지하철 노선 (예: 1호선)"),
                                                "estimatedTimeToStation", Map.of("type", "number", "description", "집에서 역까지 이동 시간(분)"),
                                                "direction", Map.of("type", "string", "description", "상행/하행"),
                                                "presetName", Map.of("type", "string", "description", "저장된 이동 시간 프리셋 이름")
                                        ),
                                        "required", new String[]{"station", "line"}
                                )
                        ),
                        Map.of(
                                "name", "save_preset",
                                "description", "집에서 역까지의 이동 시간을 프리셋으로 저장합니다.",
                                "inputSchema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "presetName", Map.of("type", "string", "description", "프리셋 이름"),
                                                "estimatedTimeToStation", Map.of("type", "number", "description", "집에서 역까지 이동 시간(분)")
                                        ),
                                        "required", new String[]{"presetName", "estimatedTimeToStation"}
                                )
                        )
                }
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTool(Map<String, Object> body) {
        Map<String, Object> params = asMap(body.get("params"), "params");
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = asMap(params.get("arguments"), "arguments");

        Object result = switch (toolName == null ? "" : toolName) {
            case "assess_departure_timing" -> departureTimingService.assess(toAssessRequest(arguments));
            case "save_preset" -> departureTimingService.savePreset(toSavePresetRequest(arguments));
            default -> throw new McpInvalidParamsException("Unknown tool: " + toolName);
        };

        return Map.of("content", new Object[]{Map.of("type", "text", "text", result.toString())});
    }

    private AssessDepartureTimingRequest toAssessRequest(Map<String, Object> args) {
        AssessDepartureTimingRequest req = new AssessDepartureTimingRequest();
        req.setStation(requireString(args, "station"));
        req.setLine(requireString(args, "line"));
        req.setDirection((String) args.get("direction"));
        req.setPresetName((String) args.get("presetName"));
        Object estimated = args.get("estimatedTimeToStation");
        if (estimated != null) {
            req.setEstimatedTimeToStation(((Number) estimated).intValue());
        }
        return req;
    }

    private SavePresetRequest toSavePresetRequest(Map<String, Object> args) {
        SavePresetRequest req = new SavePresetRequest();
        req.setPresetName(requireString(args, "presetName"));
        Object estimated = args.get("estimatedTimeToStation");
        if (estimated == null) {
            throw new McpInvalidParamsException("estimatedTimeToStation is required");
        }
        req.setEstimatedTimeToStation(((Number) estimated).intValue());
        return req;
    }

    private String requireString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new McpInvalidParamsException(key + " is required");
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o, String fieldName) {
        if (!(o instanceof Map)) {
            throw new McpInvalidParamsException(fieldName + " is missing or invalid");
        }
        return (Map<String, Object>) o;
    }

    private Map<String, Object> success(Object id, Map<String, Object> result) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("jsonrpc", "2.0");
        res.put("id", id);
        res.put("result", result);
        return res;
    }

    private Map<String, Object> error(Object id, int code, String message) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("jsonrpc", "2.0");
        res.put("id", id);
        res.put("error", Map.of("code", code, "message", message));
        return res;
    }
}