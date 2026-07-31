package com.jinwoo.mcp.departure.service;

import com.jinwoo.mcp.departure.client.ArrivalClient;
import com.jinwoo.mcp.departure.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartureTimingServiceTest {

    private DepartureTimingService service;
    private StubArrivalClient stubClient;

    @BeforeEach
    void setUp() {
        stubClient = new StubArrivalClient();
        service = new DepartureTimingService(stubClient);
    }

    @Test
    void goNow_whenDepartingNowMeansNoWait() {
        stubClient.setArrivals(List.of(5)); // 5분 뒤 도착, buffer 1분
        AssessDepartureTimingResponse res = service.assess(request(4)); // wait = 5-(4+1)=0
        assertThat(res.getDecision()).isEqualTo(Decision.GO_NOW);
    }

    @Test
    void wait_whenThereIsRoomBeforeTrainArrives() {
        stubClient.setArrivals(List.of(10));
        AssessDepartureTimingResponse res = service.assess(request(4)); // wait = 10-(4+1)=5
        assertThat(res.getDecision()).isEqualTo(Decision.WAIT);
        assertThat(res.getRecommendedDepartureTime()).isNotNull();
    }

    @Test
    void tooLate_whenNoTrainCanBeCaught() {
        stubClient.setArrivals(List.of(2)); // wait = 2-(4+1) = -3 < 0
        AssessDepartureTimingResponse res = service.assess(request(4));
        assertThat(res.getDecision()).isEqualTo(Decision.TOO_LATE);
    }

    @Test
    void wait_whenEstimatedTimeMissingAndNoPreset() {
        stubClient.setArrivals(List.of(5));
        AssessDepartureTimingResponse res = service.assess(request(null));
        assertThat(res.getDecision()).isEqualTo(Decision.WAIT);
        assertThat(res.getReason()).contains("제공해주세요");
    }

    @Test
    void wait_whenArrivalApiReturnsEmpty() {
        stubClient.setArrivals(List.of());
        AssessDepartureTimingResponse res = service.assess(request(4));
        assertThat(res.getDecision()).isEqualTo(Decision.WAIT);
        assertThat(res.getReason()).contains("실시간 열차 정보");
    }

    @Test
    void savePreset_success() {
        SavePresetRequest req = new SavePresetRequest();
        req.setPresetName("home");
        req.setEstimatedTimeToStation(8);
        assertThat(service.savePreset(req).isSuccess()).isTrue();
    }

    @Test
    void savePreset_fails_whenNameIsBlank() {
        SavePresetRequest req = new SavePresetRequest();
        req.setPresetName(" ");
        req.setEstimatedTimeToStation(8);
        assertThat(service.savePreset(req).isSuccess()).isFalse();
    }

    private AssessDepartureTimingRequest request(Integer estimated) {
        AssessDepartureTimingRequest req = new AssessDepartureTimingRequest();
        req.setStation("서울역");
        req.setLine("1호선");
        req.setEstimatedTimeToStation(estimated);
        return req;
    }

    static class StubArrivalClient implements ArrivalClient {
        private List<Integer> arrivals = List.of();

        void setArrivals(List<Integer> arrivals) {
            this.arrivals = arrivals;
        }

        @Override
        public List<Integer> getRemainingMinutes(String station, String line, String direction) {
            return arrivals;
        }
    }
}