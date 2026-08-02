# 🚇 서울시 지하철 출발 결정 도우미 MCP

지하철 탑승을 위해 **언제 출발해야 하는지**를 판단해주는 MCP 서버입니다.
사용자는 역까지 이동 시간만 제공하면, 열차 도착 시간을 고려해
**역 대기 시간을 최소화할 수 있는 출발 시점**을 추천받을 수 있습니다.

⏱️ 출근·약속 시간에 맞춰 이동할 때
"지금 나가야 할까, 조금 더 있다 나가도 될까?"를
실시간 지하철 도착 정보를 기반으로 판단해주는 도우미입니다.

## ✨ 주요 특징

- **프리셋 기능**: 출발지 기준 이동 시간을 1회 입력 후 재사용
- **간단한 입력**: 이동 시간 + 역/노선 정보만으로 판단
- **명확한 결과**: GO_NOW / WAIT / TOO_LATE 중 하나의 행동 제안
- **확장 가능 구조**: 지도 API 연동 및 혼잡도·사용자 패턴을 고려한 최적 출발 시각 추천 고도화 + 배차 간격이 짧은 케이스 커버

---

## 🚀 실행 방법

### 요구 사항

- Java 17
- 서울시 열린데이터광장 실시간 지하철 도착정보 API 키

### 로컬 실행

```bash
# 1. 환경변수 설정
export SEOUL_SUBWAY_APIKEY=발급받은_API_키    # Windows: set SEOUL_SUBWAY_APIKEY=...

# 2. 실행
./gradlew bootRun
```

서버는 기본적으로 `http://localhost:8080` 에서 실행되며, MCP 엔드포인트는 `POST /mcp` 입니다.

> API 키를 설정하지 않으면 기본값(`dummy`)이 사용되며, 실시간 조회에 실패할 경우 fallback 데이터로 동작합니다.

### 동작 확인

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

---

## 🛠 MCP Tools

### 1. `save_preset`

**용도**
사용자의 이동 시간을 프리셋에 저장합니다.

#### 📥 Parameters

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `presetName` | String | ✅ | 이동 시간을 저장할 프리셋 이름 |
| `estimatedTimeToStation` | Integer | ✅ | 역까지 이동 시간 (분) |

---

### 2. `assess_departure_timing`

**용도**
지하철을 타기 위해 **역 대기 시간을 최소화할 수 있는 최적의 출발 시점**을 판단합니다.

#### 📥 Parameters

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `station` | String | ✅ | 지하철 역 이름 |
| `line` | String | ✅ | 지하철 노선 |
| `estimatedTimeToStation` | Integer | | 역까지 이동 시간 (분) |
| `presetName` | String | | 저장된 이동 시간 프리셋 이름 |
| `direction` | String | | 상행/하행 |

> `presetName`이 제공되면 저장된 이동 시간이 사용됩니다.
> 현재 시각은 서버 기준으로 자동 처리됩니다.

#### 📤 Output

```json
{
  "decision": "GO_NOW | WAIT | TOO_LATE",
  "recommendedDepartureTime": "HH:mm or NOW",
  "reason": "string"
}
```

- `GO_NOW`: 지금 즉시 출발 권장
- `WAIT`: 추천 시각에 맞춰 출발 권장
- `TOO_LATE`: 가까운 열차 기준으로는 탑승이 어려움

> 실시간 지하철 도착 정보는 서울시 Open API를 사용하며,
> 네트워크 오류 발생 시에도 서비스가 동작하도록 fallback 데이터를 사용합니다.
> `bufferMinutes`를 설정하여 열차를 놓치는 확률을 줄였습니다.

### 📥 Example Questions

1. 집에서 신대방역까지는 10분 거리라고 저장해줘
2. 집 기준으로 신대방역 2호선 상행 방면 타려면 몇 시에 출발하는 게 좋아?
3. 지금 나가면 역에서 몇 분 정도 기다리게 돼?

---

## 🏗 프로젝트 구조

```
com.jinwoo.mcp.departure
├── controller
│   └── DepartureTimingController   # HTTP 계층 (요청 수신/응답 반환)
├── mcp
│   └── McpRequestDispatcher        # JSON-RPC 2.0 / MCP 프로토콜 처리
├── service
│   └── DepartureTimingService      # 출발 시점 판단 로직
├── client
│   ├── ArrivalClient               # 도착 정보 조회 인터페이스
│   └── SeoulArrivalClient          # 서울시 Open API 구현체
├── dto
├── exception
└── filter
```

요청 처리 흐름은 다음과 같습니다.

```
POST /mcp
  → DepartureTimingController      HTTP 요청 수신
  → McpRequestDispatcher           method 라우팅 · 파라미터 검증 · 응답 조립
  → DepartureTimingService         출발 시점 판단
  → ArrivalClient                  실시간 도착 정보 조회
```

---

## 🔧 리팩토링 (2026.07)

공모전 제출 당시 구현을 기준으로, 아래 세 가지를 개선했습니다.

### 1. JSON-RPC 프로토콜 처리와 판단 로직 분리

**기존**
Controller의 `mcp()` 메서드 하나가 method 라우팅, 파라미터 파싱, 응답 조립, 예외 처리를 모두 담당했습니다(약 150줄). 프로토콜 규격이 바뀌거나 Tool이 추가될 때마다 동일한 메서드를 계속 수정해야 했고, 판단 로직과 프로토콜 코드가 섞여 있어 테스트 대상을 분리하기 어려웠습니다.

**개선**
`McpRequestDispatcher`를 도입해 프로토콜 처리를 분리했습니다.

| 계층 | 책임 |
|---|---|
| `DepartureTimingController` | HTTP 요청 수신, 응답 반환 |
| `McpRequestDispatcher` | method 라우팅, 파라미터 검증, JSON-RPC envelope 조립 |
| `DepartureTimingService` | 출발 시점 판단 |

Controller는 3줄로 축소되었고, Tool 추가 시 디스패처의 `switch` 분기만 확장하면 됩니다.

### 2. JSON-RPC 규격에 맞는 예외 처리

**기존**
`arguments`가 누락되거나 필수 파라미터가 없으면 `NullPointerException`이 그대로 전파되어 **HTTP 500**과 Spring 기본 에러 페이지가 반환되었습니다. MCP 클라이언트 입장에서는 서버 장애와 잘못된 요청을 구분할 수 없었습니다.

```json
{
  "timestamp": "2026-07-31T07:51:06.265Z",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/mcp"
}
```

**개선**
JSON-RPC 2.0 에러 코드 체계를 적용하고, **HTTP 200 + `error` 객체** 형태로 응답하도록 변경했습니다.

| 코드 | 상황 |
|---|---|
| `-32601` | 정의되지 않은 method 호출 |
| `-32602` | 필수 파라미터 누락 또는 타입 불일치 |
| `-32603` | 그 외 서버 내부 오류 |

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "error": {
    "code": -32602,
    "message": "arguments is missing or invalid"
  }
}
```

JSON-RPC에서 에러는 전송 계층(HTTP)이 아닌 프로토콜 계층에서 표현되므로, 요청이 정상 도착한 이상 상태 코드는 200을 유지하고 응답 본문으로 원인을 전달하는 것이 규격에 부합합니다. 이를 통해 LLM 클라이언트가 실패 원인을 파악하고 사용자에게 부족한 정보를 되물을 수 있게 되었습니다.

### 3. 핵심 판단 로직 테스트 추가

**기존**
`contextLoads()` 1개. 실질적인 검증이 없어 판단 조건을 수정할 때마다 수동으로 API를 호출해 확인해야 했습니다.

**개선**
`DepartureTimingService` 단위 테스트 7개를 추가했습니다.

| 테스트 | 검증 내용 |
|---|---|
| `goNow_whenDepartingNowMeansNoWait` | 대기 시간 0분 → `GO_NOW` |
| `wait_whenThereIsRoomBeforeTrainArrives` | 여유 시간 존재 → `WAIT` + 추천 시각 반환 |
| `tooLate_whenNoTrainCanBeCaught` | 이동 시간이 도착 시간 초과 → `TOO_LATE` |
| `wait_whenEstimatedTimeMissingAndNoPreset` | 이동 시간·프리셋 모두 없음 → 안내 메시지 |
| `wait_whenArrivalApiReturnsEmpty` | 도착 정보 없음 → fallback 동작 |
| `savePreset_success` | 프리셋 저장 성공 |
| `savePreset_fails_whenNameIsBlank` | 프리셋 이름 누락 시 실패 응답 |

`ArrivalClient`를 인터페이스로 두고 테스트에서는 Stub 구현체로 대체하여, 외부 API 호출 없이 판단 로직만 독립적으로 검증할 수 있도록 했습니다.

```java
static class StubArrivalClient implements ArrivalClient {
    private List<Integer> arrivals = List.of();

    @Override
    public List<Integer> getRemainingMinutes(String station, String line, String direction) {
        return arrivals;
    }
}
```

---

## 📌 향후 개선 계획

- 프리셋의 영속 저장 (현재는 메모리 기반)
- 배차 간격이 짧은 노선에 대한 `bufferMinutes` 동적 조정
- 지도 API 연동을 통한 이동 시간 자동 산출
- 혼잡도 및 사용자 이동 패턴을 반영한 추천 고도화
