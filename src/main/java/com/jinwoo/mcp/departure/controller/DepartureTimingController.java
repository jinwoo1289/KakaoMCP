package com.jinwoo.mcp.departure.controller;

import com.jinwoo.mcp.departure.DepartureTimingMcpApplication;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingResponse;
import com.jinwoo.mcp.departure.service.DepartureTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp")
public class DepartureTimingController {
    private final DepartureTimingService departureTimingService;
    @PostMapping
    public AssessDepartureTimingResponse assess(@RequestBody AssessDepartureTimingRequest request) {
        return departureTimingService.assess(request);
    }
}
