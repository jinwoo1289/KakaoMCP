package com.jinwoo.mcp.departure.service;

import com.jinwoo.mcp.departure.client.ArrivalClient;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingRequest;
import com.jinwoo.mcp.departure.dto.AssessDepartureTimingResponse;
import com.jinwoo.mcp.departure.dto.Decision;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DepartureTimingService {
    private final Map<String, Integer> presetMap = new ConcurrentHashMap<>();
    private final ArrivalClient arrivalClient;

    public DepartureTimingService(ArrivalClient arrivalClient) {
        this.arrivalClient = arrivalClient;

        presetMap.put("home", 8);
    }

    public AssessDepartureTimingResponse assess(AssessDepartureTimingRequest request) {
        List<Integer> arrivals = arrivalClient.getRemainingMinutes(request.getStation(), request.getLine());
        Integer estimated = null;
        int bufferMinutes = 1;

        if (request.getPresetName() != null && !request.getPresetName().isBlank()) {
            estimated = presetMap.get(request.getPresetName());
        }

        if (estimated == null) {
            estimated = request.getEstimatedTimeToStation();
        }

        if (estimated == null || estimated <= 0) {
            return new AssessDepartureTimingResponse(
                    Decision.WAIT,
                    null,
                    "presetName 또는 estimatedTimeToStation(1 이상)을 제공해주세요."
            );
        }

        Integer stationWait = null;

        for (int arrivalMin: arrivals) {
            int wait = arrivalMin - (estimated + bufferMinutes);
            if (wait >= 0) {
                stationWait = wait;
                break;
            }
        }

        LocalTime currentTime = request.getCurrentTime() != null ?
                request.getCurrentTime() : LocalTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        Decision decision;
        String recommendedDepartureTime = null;
        String reason;

        if (stationWait == null) {
            decision = Decision.TOO_LATE;
            reason = "지금 출발하면 가까운 열차들 기준으로 탑승이 어렵습니다.";
        }

        else if (stationWait == 0) {
            decision = Decision.GO_NOW;
            recommendedDepartureTime = "NOW";
            reason = "지금 출발하면 역에서 대기 없이 바로 탑승할 수 있습니다.";
        }
        else {
            decision = Decision.WAIT;
            recommendedDepartureTime = currentTime.plusMinutes(stationWait).format(fmt);
            reason = "지금 출발하면 역에서 약 " + stationWait + "분 대기합니다. "
                    + recommendedDepartureTime + "에 출발을 추천합니다.";
        }


        return new AssessDepartureTimingResponse(decision, recommendedDepartureTime, reason);
    }
}
