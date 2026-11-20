package model;

/**
 * 좌석의 공통 부모 클래스 (추상 클래스).
 * 좌석 종류마다 달라지는 부분(종류 이름, 최대 이용 시간)은
 * 자식 클래스가 오버라이드하도록 추상 메소드로 선언한다.
 */
public abstract class Seat {
    private final String id;          // 좌석 번호 (예: N01, F03, R1)
    private final int hourlyRate;     // 시간당 요금(원)

    protected Seat(String id, int hourlyRate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("좌석 번호는 비어 있을 수 없습니다.");
        }
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("요금은 0 이상이어야 합니다.");
        }
        this.id = id;
        this.hourlyRate = hourlyRate;
    }

    public String getId() {
        return id;
    }

    public int getHourlyRate() {
        return hourlyRate;
    }

    /** 좌석 종류 이름 (예: "일반석") */
    public abstract String getType();

    /** 1회 예약 시 최대 이용 가능 시간(시간 단위) */
    public abstract int getMaxHours();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        return id.equals(((Seat) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | 시간당 %,d원 | 최대 %d시간",
                id, getType(), hourlyRate, getMaxHours());
    }
}
