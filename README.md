# 🚇 서울시 지하철 출발 결정 도우미 MCP


지하철 탑승을 위해 **언제 출발해야 하는지**를 판단해주는 MCP 서버입니다.  
사용자는 역까지 이동 시간만 제공하면, 열차 도착 시간을 고려해  
**역 대기 시간을 최소화할 수 있는 출발 시점**을 추천받을 수 있습니다.

⏱️ 출근·약속 시간에 맞춰 이동할 때  
“지금 나가야 할까, 조금 더 있다 나가도 될까?”를  
실시간 지하철 도착 정보를 기반으로 판단해주는 도우미입니다.



## ✨ 주요 특징
- **프리셋 기능**: 출발지 기준 이동 시간을 1회 입력 후 재사용
- **간단한 입력**: 이동 시간 + 역/노선 정보만으로 판단
- **명확한 결과**: GO_NOW / WAIT / TOO_LATE 중 하나의 행동 제안
- **확장 가능 구조**: 지도 API 연동 및 혼잡도·사용자 패턴을 고려한 최적 출발 시각 추천 고도화 + 배차 간격이 짧은 케이스 커버

---

## 🛠 MCP Tools

### 1. `SavePreset`

**용도**
사용자의 이동 시간을 프리셋에 저장합니다.

---

### 📥 Parameters
| 이름 | 타입 | 설명 |
|---|---|---|
| `presetName` | String | 이동 시간을 저장할 프리셋 이름 |
| `estimatedTimeToStation` | Integer | 역까지 이동 시간 (분) |

---

### 2. `assessDepartureTiming`

**용도**  
지하철을 타기 위해 **역 대기 시간을 최소화할 수 있는 최적의 출발 시점**을 판단합니다.

---

### 📥 Parameters
| 이름 | 타입 | 설명 |
|---|---|---|
| `presetName` | String | 저장된 이동 시간 프리셋 이름 |
| `estimatedTimeToStation` | Integer | 역까지 이동 시간 (분) |
| `station` | String | 지하철 역 이름 |
| `line` | String | 지하철 노선 |
| `currentTime` | String | 현재 시각 (HH:mm) |

> `presetName`이 제공되면 해당 값이 우선 사용됩니다.
> `currentTime`은 제공되지 않을 경우 서버 기준 현재 시각으로 자동 처리됩니다.


---

### 📤 Output
```json
{
  "decision": "GO_NOW | WAIT | TOO_LATE",
  "recommendedDepartureTime": "HH:mm or NOW",
  "reason": "string"
}
```

- GO_NOW: 지금 즉시 출발 권장
- WAIT: 추천 시각에 맞춰 출발 권장
- TOO_LATE: 가까운 열차 기준으로는 탑승이 어려움


> 실시간 지하철 도착 정보는 서울시 Open API를 사용하며,  
> 네트워크 오류 발생 시에도 서비스가 동작하도록 fallback 데이터를 사용합니다.



📥 Example Questions

1. 집에서 신대방역까지는 10분 거리라고 저장해줘

2. 집 기준으로 신대방역 2호선 타려면 몇 시에 출발하는 게 좋아?

3. 지금 나가면 역에서 몇 분 정도 기다리게 돼?

---
