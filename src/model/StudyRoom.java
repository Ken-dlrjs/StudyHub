package model;

/** 스터디룸: 최소 2인 이상이 이용하며, 수용 인원 필드를 추가로 가진다. */
public class StudyRoom extends Seat {
    public static final int MAX_HOURS = 4;
    public static final int DEFAULT_RATE = 6000;

    private final int capacity; // 수용 인원

    public StudyRoom(String id, int capacity) {
        super(id, DEFAULT_RATE);
        if (capacity < 2) {
            throw new IllegalArgumentException("스터디룸 수용 인원은 2명 이상이어야 합니다.");
        }
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String getType() {
        return "스터디룸";
    }

    @Override
    public int getMaxHours() {
        return MAX_HOURS;
    }

    @Override
    public String toString() {
        return super.toString() + " | 수용 " + capacity + "명";
    }
}
