package model;

import exception.DataFileException;
import io.CsvConvertible;
import service.MemberService;
import service.SeatService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 예약 한 건. 상태 변경(입실·퇴실·취소·변경)은 반드시 이 클래스의 메소드를 거치며,
 * 현재 상태에서 할 수 없는 동작이면 IllegalStateException을 던진다.
 */
public class Reservation implements CsvConvertible {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String id;           // 예약 번호 (예: B0001)
    private final Member member;
    private Seat seat;
    private LocalDateTime start;
    private LocalDateTime end;
    private ReservationStatus status;
    private LocalDateTime checkInTime;  // 실제 입실 시각
    private LocalDateTime checkOutTime; // 실제 퇴실 시각

    public Reservation(String id, Member member, Seat seat, LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 뒤여야 합니다.");
        }
        this.id = id;
        this.member = member;
        this.seat = seat;
        this.start = start;
        this.end = end;
        this.status = ReservationStatus.RESERVED;
    }

    /** 두 예약의 시간대가 겹치는가? (좌석은 비교하지 않는다) */
    public boolean overlapsTime(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    /** 특정 시각에 이 예약이 좌석을 차지하고 있는가? */
    public boolean occupiesAt(LocalDateTime time) {
        return status.isActive() && !time.isBefore(start) && time.isBefore(end);
    }

    public void checkIn(LocalDateTime time) {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("입실할 수 없는 상태입니다: " + status.getLabel());
        }
        status = ReservationStatus.CHECKED_IN;
        checkInTime = time;
    }

    /** 퇴실 처리 후 실제 이용 시간(분)을 반환한다. 최소 1분으로 계산한다. */
    public int checkOut(LocalDateTime time) {
        if (status != ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("퇴실할 수 없는 상태입니다: " + status.getLabel());
        }
        status = ReservationStatus.COMPLETED;
        checkOutTime = time;
        return getUsedMinutes();
    }

    public void cancel() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("취소할 수 없는 상태입니다: " + status.getLabel());
        }
        status = ReservationStatus.CANCELED;
    }

    /** 입실하지 않고 예약 시간이 끝난 예약을 노쇼로 바꾼다. */
    public void markNoShow() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("노쇼 처리할 수 없는 상태입니다: " + status.getLabel());
        }
        status = ReservationStatus.NO_SHOW;
    }

    /** 좌석·시간 변경. 충돌 검사는 서비스에서 먼저 끝낸 뒤 호출한다. */
    public void change(Seat newSeat, LocalDateTime newStart, LocalDateTime newEnd) {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("변경할 수 없는 상태입니다: " + status.getLabel());
        }
        if (!newStart.isBefore(newEnd)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 뒤여야 합니다.");
        }
        this.seat = newSeat;
        this.start = newStart;
        this.end = newEnd;
    }

    /** 예약된 시간(시간 단위) */
    public int getHours() {
        return (int) Duration.between(start, end).toHours();
    }

    /** 실제 이용 시간(분). 퇴실 전이면 0 */
    public int getUsedMinutes() {
        if (checkInTime == null || checkOutTime == null) return 0;
        return (int) Math.max(1, Duration.between(checkInTime, checkOutTime).toMinutes());
    }

    public String getId() { return id; }
    public Member getMember() { return member; }
    public Seat getSeat() { return seat; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }

    // ---------------- CSV 변환 ----------------

    @Override
    public String[] toCsvFields() {
        return new String[]{
                id, member.getId(), seat.getId(), start.toString(), end.toString(), status.name(),
                checkInTime == null ? "" : checkInTime.toString(),
                checkOutTime == null ? "" : checkOutTime.toString()
        };
    }

    /**
     * CSV 필드로 예약을 복원한다. 회원·좌석 번호로 실제 객체를 찾아 연결한다.
     * 상태와 입실·퇴실 시각이 서로 맞지 않으면 DataFileException
     */
    public static Reservation fromCsv(String[] f, MemberService members, SeatService seats) {
        if (f.length != 8) {
            throw new DataFileException("예약 필드 수가 8개가 아닙니다. (" + f.length + "개)");
        }
        try {
            Member member = members.find(f[1].trim());
            Seat seat = seats.find(f[2].trim());
            Reservation r = new Reservation(f[0].trim(), member, seat,
                    LocalDateTime.parse(f[3].trim()), LocalDateTime.parse(f[4].trim()));
            r.status = parseStatus(f[5].trim());
            r.checkInTime = f[6].isBlank() ? null : LocalDateTime.parse(f[6].trim());
            r.checkOutTime = f[7].isBlank() ? null : LocalDateTime.parse(f[7].trim());
            r.validateRestoredState();
            return r;
        } catch (DateTimeParseException e) {
            throw new DataFileException("날짜·시간 형식이 올바르지 않습니다: " + e.getParsedString());
        }
    }

    private static ReservationStatus parseStatus(String text) {
        for (ReservationStatus s : ReservationStatus.values()) {
            if (s.name().equals(text)) return s;
        }
        throw new DataFileException("알 수 없는 예약 상태입니다: " + text);
    }

    private void validateRestoredState() {
        boolean hasIn = checkInTime != null;
        boolean hasOut = checkOutTime != null;
        boolean ok = switch (status) {
            case RESERVED, CANCELED, NO_SHOW -> !hasIn && !hasOut;
            case CHECKED_IN -> hasIn && !hasOut;
            case COMPLETED -> hasIn && hasOut && !checkOutTime.isBefore(checkInTime);
        };
        if (!ok) {
            throw new DataFileException("예약 상태(" + status + ")와 입실·퇴실 시각이 맞지 않습니다.");
        }
    }

    @Override
    public String toString() {
        String s = String.format("[%s] %s(%s) | %s %s | %s ~ %s (%d시간) | %s",
                id, member.getName(), member.getId(), seat.getId(), seat.getType(),
                start.format(FMT), end.format(FMT).substring(11), getHours(), status.getLabel());
        if (status == ReservationStatus.COMPLETED) {
            s += String.format(" | 실제 이용 %d분", getUsedMinutes());
        }
        return s;
    }
}
