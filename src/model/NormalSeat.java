package model;

/** 일반석: 최대 6시간까지 예약할 수 있다. */
public class NormalSeat extends Seat {
    public static final int MAX_HOURS = 6;
    public static final int DEFAULT_RATE = 1500;

    public NormalSeat(String id) {
        super(id, DEFAULT_RATE);
    }

    @Override
    public String getType() {
        return "일반석";
    }

    @Override
    public int getMaxHours() {
        return MAX_HOURS;
    }
}
