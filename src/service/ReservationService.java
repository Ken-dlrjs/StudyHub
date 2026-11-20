package service;

import exception.InsufficientTimeException;
import exception.NotFoundException;
import exception.ReservationConflictException;
import model.Member;
import model.Reservation;
import model.ReservationStatus;
import model.Seat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 예약 생성·변경·취소, 입실·퇴실을 담당한다.
 * 규칙: 운영 09:00~24:00, 1시간 단위 예약, 좌석 종류별 최대 이용 시간 제한.
 * 현재 시각은 Clock에서 얻는다. (테스트 시 시각을 고정할 수 있도록 생성자로 주입)
 */
public class ReservationService {
    public static final int OPEN_HOUR = 9;
    public static final int CLOSE_HOUR = 24;
    public static final int EARLY_CHECKIN_MINUTES = 10; // 시작 10분 전부터 입실 가능

    private final List<Reservation> reservations = new ArrayList<>();
    private final Clock clock;

    public ReservationService() {
        this(Clock.systemDefaultZone());
    }

    public ReservationService(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // ---------------- 예약 생성 / 변경 / 취소 ----------------

    public Reservation reserve(Member member, Seat seat, LocalDate date, int startHour, int hours) {
        LocalDateTime start = date.atTime(startHour, 0);
        LocalDateTime end = start.plusHours(hours);
        validateTime(seat, date, startHour, hours, start);
        checkConflict(member, seat, start, end, null);
        checkRemainingTime(member, hours, null);

        Reservation r = new Reservation(nextId(), member, seat, start, end);
        reservations.add(r);
        return r;
    }

    public Reservation change(String reservationId, Seat newSeat, LocalDate date, int startHour, int hours) {
        Reservation r = find(reservationId);
        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("예약 상태일 때만 변경할 수 있습니다. (현재: " + r.getStatus().getLabel() + ")");
        }
        LocalDateTime start = date.atTime(startHour, 0);
        LocalDateTime end = start.plusHours(hours);
        validateTime(newSeat, date, startHour, hours, start);
        // 자기 자신과는 충돌하지 않도록 제외하고 다시 검사한다.
        checkConflict(r.getMember(), newSeat, start, end, r.getId());
        checkRemainingTime(r.getMember(), hours, r.getId());
        r.change(newSeat, start, end);
        return r;
    }

    public void cancel(String reservationId) {
        find(reservationId).cancel();
    }

    // ---------------- 입실 / 퇴실 ----------------

    public Reservation checkIn(String reservationId) {
        Reservation r = find(reservationId);
        LocalDateTime now = now();
        if (now.isBefore(r.getStart().minusMinutes(EARLY_CHECKIN_MINUTES))) {
            throw new IllegalStateException("아직 입실할 수 없습니다. 시작 "
                    + EARLY_CHECKIN_MINUTES + "분 전부터 입실 가능합니다.");
        }
        if (!now.isBefore(r.getEnd())) {
            throw new IllegalStateException("예약 시간이 이미 지났습니다.");
        }
        r.checkIn(now);
        return r;
    }

    /** 퇴실 처리 후 실제 이용 시간만큼 이용권을 차감한다. 차감한 시간(분)을 반환한다. */
    public int checkOut(String reservationId) {
        Reservation r = find(reservationId);
        int used = r.checkOut(now());
        Member m = r.getMember();
        int deducted = Math.min(used, m.getRemainingMinutes());
        m.setRemainingMinutes(m.getRemainingMinutes() - deducted);
        return deducted;
    }

    // ---------------- 조회 ----------------

    public Reservation find(String id) {
        for (Reservation r : reservations) {
            if (r.getId().equalsIgnoreCase(id.trim())) {
                return r;
            }
        }
        throw new NotFoundException("존재하지 않는 예약입니다: " + id);
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>(reservations);
        list.sort(Comparator.comparing(Reservation::getStart));
        return list;
    }

    public List<Reservation> findByMember(String memberId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : findAll()) {
            if (r.getMember().getId().equals(memberId)) result.add(r);
        }
        return result;
    }

    public List<Reservation> findByDate(LocalDate date) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : findAll()) {
            if (r.getStart().toLocalDate().equals(date)) result.add(r);
        }
        return result;
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : findAll()) {
            if (r.getStatus() == status) result.add(r);
        }
        return result;
    }

    /** 특정 시각에 사용(예약) 중인 좌석 번호 집합. 배치도 표시에 사용한다. */
    public Set<String> occupiedSeatIds(LocalDateTime time) {
        Set<String> ids = new HashSet<>();
        for (Reservation r : reservations) {
            if (r.occupiesAt(time)) ids.add(r.getSeat().getId());
        }
        return ids;
    }

    /** 회원에게 진행 중(예약·이용 중)인 예약이 있는가? 회원 삭제 전 확인용 */
    public boolean hasActiveReservation(String memberId) {
        for (Reservation r : reservations) {
            if (r.getMember().getId().equals(memberId) && r.getStatus().isActive()) return true;
        }
        return false;
    }

    // ---------------- 내부 검사 로직 ----------------

    private void validateTime(Seat seat, LocalDate date, int startHour, int hours, LocalDateTime start) {
        if (startHour < OPEN_HOUR || startHour >= CLOSE_HOUR) {
            throw new IllegalArgumentException("시작 시간은 " + OPEN_HOUR + "시 ~ " + (CLOSE_HOUR - 1) + "시 사이여야 합니다.");
        }
        if (hours < 1) {
            throw new IllegalArgumentException("이용 시간은 1시간 이상이어야 합니다.");
        }
        if (startHour + hours > CLOSE_HOUR) {
            throw new IllegalArgumentException("운영 종료(24시) 이후까지는 예약할 수 없습니다.");
        }
        // 다형성: 좌석 종류를 몰라도 각 좌석이 자신의 최대 시간을 알려준다.
        if (hours > seat.getMaxHours()) {
            throw new IllegalArgumentException(seat.getType() + "은(는) 최대 " + seat.getMaxHours() + "시간까지 예약할 수 있습니다.");
        }
        if (start.isBefore(now())) {
            throw new IllegalArgumentException("지난 시간에는 예약할 수 없습니다.");
        }
    }

    /** 같은 좌석에 겹치는 예약, 또는 같은 회원이 같은 시간에 다른 좌석을 예약했는지 검사한다. */
    private void checkConflict(Member member, Seat seat, LocalDateTime start, LocalDateTime end, String excludeId) {
        for (Reservation r : reservations) {
            if (!r.getStatus().isActive() || r.getId().equals(excludeId)) continue;
            if (!r.overlapsTime(start, end)) continue;
            if (r.getSeat().equals(seat)) {
                throw new ReservationConflictException("해당 시간에 이미 예약된 좌석입니다: " + r);
            }
            if (r.getMember().equals(member)) {
                throw new ReservationConflictException("같은 시간에 이미 다른 좌석을 예약했습니다: " + r);
            }
        }
    }

    /** 잔여 이용권이 (이미 잡힌 다른 예약 시간 + 이번 예약 시간)보다 적으면 예외 */
    private void checkRemainingTime(Member member, int hours, String excludeId) {
        int reserved = 0;
        for (Reservation r : reservations) {
            if (r.getMember().equals(member) && r.getStatus() == ReservationStatus.RESERVED
                    && !r.getId().equals(excludeId)) {
                reserved += r.getHours() * 60;
            }
        }
        int needed = reserved + hours * 60;
        if (member.getRemainingMinutes() < needed) {
            throw new InsufficientTimeException(String.format(
                    "이용권 시간이 부족합니다. (잔여 %d분, 기존 예약 %d분 + 이번 예약 %d분 필요)",
                    member.getRemainingMinutes(), reserved, hours * 60));
        }
    }

    /** B0001, B0002 ... (현재 최대 번호 + 1) */
    private String nextId() {
        int max = 0;
        for (Reservation r : reservations) {
            try {
                max = Math.max(max, Integer.parseInt(r.getId().substring(1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("B%04d", max + 1);
    }
}
