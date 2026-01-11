package com.jinwoo.mcp.departure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "KakaoMCP - Departure Timing API is running. Use POST /mcp and POST /mcp/presets";
    }
}
