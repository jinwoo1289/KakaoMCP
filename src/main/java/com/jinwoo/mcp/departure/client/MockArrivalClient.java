package com.jinwoo.mcp.departure.client;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockArrivalClient implements ArrivalClient {

    @Override
    public List<Integer> getRemainingMinutes(String station, String line) {
        return List.of(5, 12); // 임의의 값
    }
}
