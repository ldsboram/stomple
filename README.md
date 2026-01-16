# Stomple

**Stomple**은 7×7 보드에서 진행되는 **턴제 전략 보드게임** 모바일 앱입니다.
플레이어는 컴퓨터와 번갈아 가며 구슬을 제거하며, 상대가 더 이상 수를 둘 수 없도록 만드는 것이 목표입니다.

---

## 🎮 Gameplay Overview

* 7×7 보드 위에서 게임 진행
* 다양한 색상의 구슬을 밟아 제거
* 가능한 수를 전략적으로 줄여 상대를 봉쇄
* 연속 제거(Chain Stomp)를 통한 고득점 플레이
* 게임 종료 시 점수 계산

**목표**
상대의 선택지를 모두 제거하여 승리하고, 최대한 높은 점수를 기록하세요.

---

## 🧠 Core Features

* **Turn-based Strategy**

  * 사용자 vs 컴퓨터 AI
* **Chain Stomp System**

  * 동일 색상의 인접한 구슬을 연속 제거 가능
* **Score System**

  * 생존 시간과 플레이 결과에 따른 점수 산정
* **Ranking System**

  * 온라인 랭킹 서버 연동
  * 점수 제출 및 전체 순위 확인 가능

---

## 📱 App Structure

### Main Screen

* 게임 시작
* 게임 규칙 확인

### Game Screen

* 동적으로 생성되는 7×7 그리드
* 현재 선택 가능한 위치 강조 표시
* 실시간 게임 상태 표시

### Result Screen

* 최종 점수 및 결과 표시
* 점수 서버 제출 (선택)
* 전체 랭킹 조회 가능

---

## 🌐 Online Ranking

* 게임 종료 후 점수를 서버에 제출할 수 있습니다.
* 전체 플레이어 랭킹을 확인하며 경쟁할 수 있습니다.
* 점수 제출은 선택 사항입니다.

---

## 🛠 Technical Overview

* **Platform**: Android
* **Language**: Kotlin
* **UI**

  * 동적 Grid Layout
  * 다양한 화면 크기 대응
* **Server**

  * Node.js + Express
  * SQLite3
  * REST API 기반 점수 제출 및 랭킹 조회

---

## 🎨 Design Philosophy

* 불필요한 요소를 제거한 **심플하고 직관적인 UI**
* 규칙을 빠르게 이해하고 바로 플레이 가능
* 전략과 판단에 집중할 수 있는 화면 구성

---

## 📌 Notes

* 싱글 플레이 전략 보드게임
* 온라인 랭킹을 통한 경쟁 요소 제공
* 오프라인 플레이 가능 (랭킹 기능 제외)


