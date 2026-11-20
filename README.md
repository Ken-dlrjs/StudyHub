# StudyHub

스터디 카페/독서실 좌석 예약 관리 시스템 (Java 콘솔 프로그램)
문제해결및실습: JAVA (009956-004) 개인 프로젝트

## 문제 · 목표
좌석 현황을 수기로 관리하면 중복 예약이 생기고 이용 시간 집계가 어렵습니다.
회원·좌석·예약을 관리하고, 시간 충돌을 자동으로 검사하며, 이용 통계를 제공하는 프로그램을 만듭니다.

## 주요 기능 (예정)
1. 회원 관리 (등록·조회·수정·삭제)
2. 좌석 현황 조회
3. 예약 생성 (시간 충돌 검사)
4. 예약 조회·변경·취소
5. 입실·퇴실 처리
6. 통계 (좌석 이용률, 회원별 이용 시간, 시간대별 혼잡도)
7. CSV 파일 저장·불러오기

## 프로그램 구조
```
src/
 ├─ Main.java
 ├─ model/       Seat, NormalSeat, FocusSeat, StudyRoom, Member, Reservation
 ├─ service/     MemberService, SeatService, ReservationService, StatisticsService
 ├─ io/          CsvConvertible, FileManager
 ├─ exception/   사용자 정의 예외
 └─ ui/          ConsoleMenu
data/            CSV 데이터 파일
```

## 개발 환경
- JDK 17
- IntelliJ IDEA

## 실행 방법
(작성 예정)

## 테스트 기록
(작성 예정)
