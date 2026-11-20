package service;

import exception.NotFoundException;
import model.FocusSeat;
import model.NormalSeat;
import model.Seat;
import model.StudyRoom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 좌석 정보를 관리한다. 좌석은 프로그램 시작 시 고정 구성으로 생성한다.
 * LinkedHashMap을 사용해 등록한 순서(일반석 -> 집중석 -> 스터디룸)를 유지한다.
 */
public class SeatService {
    public static final int NORMAL_COUNT = 20;  // N01 ~ N20
    public static final int FOCUS_COUNT = 6;    // F01 ~ F06
    public static final int ROOM_COUNT = 3;     // R1 ~ R3
    public static final int ROOM_CAPACITY = 4;

    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public SeatService() {
        initDefaultSeats();
    }

    private void initDefaultSeats() {
        for (int i = 1; i <= NORMAL_COUNT; i++) {
            add(new NormalSeat(String.format("N%02d", i)));
        }
        for (int i = 1; i <= FOCUS_COUNT; i++) {
            add(new FocusSeat(String.format("F%02d", i)));
        }
        for (int i = 1; i <= ROOM_COUNT; i++) {
            add(new StudyRoom("R" + i, ROOM_CAPACITY));
        }
    }

    private void add(Seat seat) {
        seats.put(seat.getId(), seat);
    }

    public Seat find(String id) {
        Seat seat = seats.get(id.trim().toUpperCase());
        if (seat == null) {
            throw new NotFoundException("존재하지 않는 좌석입니다: " + id);
        }
        return seat;
    }

    public List<Seat> findAll() {
        return new ArrayList<>(seats.values());
    }

    /** 특정 종류(NormalSeat.class 등)의 좌석만 골라 반환한다. */
    public List<Seat> findByType(Class<? extends Seat> type) {
        List<Seat> result = new ArrayList<>();
        for (Seat s : seats.values()) {
            if (type.isInstance(s)) {
                result.add(s);
            }
        }
        return result;
    }

    public int count() {
        return seats.size();
    }
}
