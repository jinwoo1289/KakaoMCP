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
     * ✅ PlayMCP "정보 불러오기" / 연결 확인용
     * - 어떤 메서드/Content-Type/바디가 와도 OK를 돌려줘야 연결 실패가 안 뜸
     * - PlayMCP가 endpoint에 /mcp를 붙이거나 또 붙여서 /mcp/mcp 로 때리는 경우도 있어서 같이 열어둠
     */
    @RequestMapping(
            value = {"/", "/mcp", "/mcp/", "/mcp/mcp"},
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.HEAD},
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> validate(
            @RequestBody(required = false) String rawBody,
            @RequestHeader Map<String, String> headers
    ) {
        // ✅ 디버그: PlayMCP가 뭘 보내는지 확인
        log.warn("MCP_VALIDATE path validate-hit, rawBody={}", rawBody);
        log.warn("MCP_VALIDATE headers={}", headers);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "MCP server validated"
        ));
    }

    /**
     * ✅ 실제 기능 요청 (Postman/LLM Tool 호출은 여기로)
     * - PlayMCP 검증과 섞이면 계속 실패하니까 분리
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
     * ✅ preset 저장도 유지
     */
    @PostMapping(
            value = "/mcp/presets",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> savePreset(@RequestBody SavePresetRequest request) {
        // 기존 SavePresetRequest/Response 쓰고 있으면 그 타입으로 바꿔도 됨
        return ResponseEntity.ok(departureTimingService.savePreset(request));
    }
}
