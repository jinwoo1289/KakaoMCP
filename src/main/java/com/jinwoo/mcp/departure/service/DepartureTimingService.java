package com.jinwoo.mcp.departure.service;

import com.jinwoo.mcp.departure.client.ArrivalClient;
import com.jinwoo.mcp.departure.dto.*;
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

//        presetMap.put("home", 8); // 테스트 값
    }

    public AssessDepartureTimingResponse assess(AssessDepartureTimingRequest request) {
        List<Integer> arrivals = arrivalClient.getRemainingMinutes(request.getStation(), request.getLine(),
                request.getDirection());
        Integer estimated = null;
        int bufferMinutes = 1;

        if (arrivals.isEmpty()) {
            return new AssessDepartureTimingResponse(
                    Decision.WAIT,
                    null,
                    "실시간 열차 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
            );
        }

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
            reason = "현재 도착 예정인 열차들로는 탑승이 어렵습니다. 다음 열차 정보 갱신을 위해 잠시 후에 다시 시도해 주세요";
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

    public SavePresetResponse savePreset(SavePresetRequest request) {
        if (request.getPresetName() == null || request.getPresetName().isBlank()) {
            return new SavePresetResponse(false, "프리셋 이름을 입력해주세요.");
        }
        if (request.getEstimatedTimeToStation() == null || request.getEstimatedTimeToStation() <= 0) {
            return new SavePresetResponse(false, "1분 이상의 시간을 입력해 주세요.");
        }

        presetMap.put(request.getPresetName(), request.getEstimatedTimeToStation());
        return new SavePresetResponse(true,
                "프리셋 '" + request.getPresetName() + "' 저장 완료");
    }
}
