package model;

/** 1인 집중석: 요금이 조금 더 비싸고, 오래 이용할 수 있다(최대 10시간). */
public class FocusSeat extends Seat {
    public static final int MAX_HOURS = 10;
    public static final int DEFAULT_RATE = 2000;

    public FocusSeat(String id) {
        super(id, DEFAULT_RATE);
    }

    @Override
    public String getType() {
        return "집중석";
    }

    @Override
    public int getMaxHours() {
        return MAX_HOURS;
    }
}
