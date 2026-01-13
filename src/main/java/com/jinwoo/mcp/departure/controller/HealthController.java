package com.jinwoo.mcp.departure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "name", "departure-timing-mcp",
                "status", "ok",
                "endpoints", List.of("/mcp/assess", "/mcp/presets")
        );
    }

    // HEAD / 도 같이 처리(어떤 클라이언트는 HEAD로 체크)
    @RequestMapping(value="/", method= RequestMethod.HEAD)
    public void headRoot() {}

    @GetMapping("/mcp")
    public Map<String, Object> mcpInfo() {
        return Map.of(
                "name", "departure-timing-mcp",
                "version", "1.0.0",
                "description", "지하철 출발 타이밍 추천 MCP",
                "tools", List.of("departureTiming") // 너 tool 이름 맞춰
        );
    }

    @RequestMapping(value="/mcp", method=RequestMethod.HEAD)
    public void headMcp() {}

}
