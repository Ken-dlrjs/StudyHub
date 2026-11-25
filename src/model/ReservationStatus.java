package model;

/**
 * 예약 상태. RESERVED -> CHECKED_IN -> COMPLETED,
 * 또는 RESERVED -> CANCELED(직접 취소) / NO_SHOW(입실하지 않고 예약 시간이 끝남)
 */
public enum ReservationStatus {
    RESERVED("예약"),
    CHECKED_IN("이용 중"),
    COMPLETED("이용 완료"),
    CANCELED("취소"),
    NO_SHOW("노쇼");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 좌석을 차지하고 있는 상태인가? (충돌 검사 대상) */
    public boolean isActive() {
        return this == RESERVED || this == CHECKED_IN;
    }
}
