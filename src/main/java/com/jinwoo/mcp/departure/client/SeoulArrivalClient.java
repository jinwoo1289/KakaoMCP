package com.jinwoo.mcp.departure.client;


import com.jinwoo.mcp.departure.dto.RealTimeArrivalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
@Slf4j
public class SeoulArrivalClient implements ArrivalClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${seoul.subway.apiKey}")
    private String apiKey;

    @Override
    public List<Integer> getRemainingMinutes(String station, String line, String direction) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("http://swopenAPI.seoul.go.kr/api/subway/{key}/xml/realtimeStationArrival/0/30/{station}")
                    .buildAndExpand(Map.of("key", apiKey, "station", station))
                    .encode()
                    .toUri();

            RealTimeArrivalResponse res =
                    restTemplate.getForObject(uri, RealTimeArrivalResponse.class);

            if (res == null || res.getRow() == null || res.getRow().isEmpty()) {
                return mockArrivals(); // fallback
            }

            String targetSubwayId = lineToSubwayId(line);
            if (targetSubwayId == null)
                return mockArrivals();

            List<Integer> live = res.getRow().stream()
                    .filter(r -> targetSubwayId.equals(r.getSubwayId()))
                    .filter(r -> direction == null || direction.equals(r.getUpdnLine()))
                    .map(r -> parseSecondsToMinutesCeil(r.getBarvlDt()))
                    .filter(Objects::nonNull)
                    .filter(m -> m > 0)
                    .sorted()
                    .limit(5)
                    .toList();

            log.info("LIVE size={} station={} line={} ids={}",
                    live.size(), station, line,
                    res.getRow().stream().map(RealTimeArrivalResponse.Row::getSubwayId).distinct().toList()
            );

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

    private String lineToSubwayId(String line) {
        if (line == null) return null;
        String s = line.trim();

        return switch (s) {
            case "1", "1호선" -> "1001";
            case "2", "2호선" -> "1002";
            case "3", "3호선" -> "1003";
            case "4", "4호선" -> "1004";
            case "5", "5호선" -> "1005";
            case "6", "6호선" -> "1006";
            case "7", "7호선" -> "1007";
            case "8", "8호선" -> "1008";
            case "9", "9호선" -> "1009";

            case "중앙선" -> "1061";
            case "경의중앙선" -> "1063";
            case "공항철도", "AREX" -> "1065";
            case "경춘선" -> "1067";
            case "수인분당선", "분당선", "수인선" -> "1075";
            case "신분당선" -> "1077";
            case "우이신설선" -> "1092";
            case "서해선" -> "1093";
            case "경강선" -> "1081";
            case "GTX-A", "GTXA" -> "1032";

            default -> null;
        };
    }


}
