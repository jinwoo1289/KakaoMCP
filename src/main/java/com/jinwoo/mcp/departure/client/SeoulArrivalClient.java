package com.jinwoo.mcp.departure.client;


import com.jinwoo.mcp.departure.dto.RealTimeArrivalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class SeoulArrivalClient implements ArrivalClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${seoul.subway.apiKey}")
    private String apiKey;

    @Override
    public List<Integer> getRemainingMinutes(String station, String line) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("http://swopenAPI.seoul.go.kr/api/subway/{key}/xml/realtimeStationArrival/0/30/{station}")
                    .buildAndExpand(apiKey, station)
                    .encode()
                    .toUri();

            RealTimeArrivalResponse res =
                    restTemplate.getForObject(uri, RealTimeArrivalResponse.class);

            if (res == null || res.getRow() == null || res.getRow().isEmpty()) {
                return mockArrivals(); // fallback
            }

            String normalizedLine = line.endsWith("호선") ? line : line + "호선";

            List<Integer> live = res.getRow().stream()
                    // station 필터는 사실 필요없어서 빼도 됨(원하면 유지)
                    .filter(r -> r.getTrainLineNm() != null && r.getTrainLineNm().contains(normalizedLine))
                    .map(r -> parseSecondsToMinutesCeil(r.getBarvlDt()))
                    .filter(Objects::nonNull)
                    .sorted()
                    .limit(5)
                    .toList();

            if (live.isEmpty()) {
                System.out.println("[SeoulArrivalClient] LIVE size=" + live.size() + " station=" + station
                + " line=" + line + " -> " + live);
            }

            return live.isEmpty() ? mockArrivals() : live;

        } catch (Exception e) {
            System.out.println("[SeoulArrivalClient] FALLBACK used: " + e.getMessage());
            return mockArrivals(); // 네트워크/파싱 에러도 안정 fallback
        }
    }

    private List<Integer> mockArrivals() {
        return List.of(2, 5, 8, 11, 14);
    }

    private Integer parseSecondsToMinutesCeil(String secondsStr) {
        if (secondsStr == null) return null;
        secondsStr = secondsStr.trim();
        if (!secondsStr.matches("\\d+")) return null;

        int sec = Integer.parseInt(secondsStr);

        return (sec == 0) ? 0 : (int) Math.ceil(sec / 60.0);
    }
}
