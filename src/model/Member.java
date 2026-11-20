package model;

/**
 * 회원 정보. 모든 필드는 private이며, setter에서 값을 검증한다.
 * 잔여 이용 시간은 '분' 단위로 저장한다. (퇴실 시 실제 이용 시간을 정확히 차감하기 위함)
 */
public class Member {
    private final String id;         // 회원 번호 (예: M001)
    private String name;
    private String phone;
    private int remainingMinutes;    // 잔여 이용권(분)

    public Member(String id, String name, String phone, int remainingMinutes) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("회원 번호는 비어 있을 수 없습니다.");
        }
        this.id = id;
        setName(name);
        setPhone(phone);
        setRemainingMinutes(remainingMinutes);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    /** 전화번호는 숫자와 하이픈만 허용하며, 숫자는 9~11자리여야 한다. */
    public void setPhone(String phone) {
        if (phone == null || !phone.matches("[0-9-]+")) {
            throw new IllegalArgumentException("전화번호는 숫자와 '-'만 입력할 수 있습니다.");
        }
        int digits = phone.replace("-", "").length();
        if (digits < 9 || digits > 11) {
            throw new IllegalArgumentException("전화번호 자릿수가 올바르지 않습니다.");
        }
        this.phone = phone;
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(int remainingMinutes) {
        if (remainingMinutes < 0) {
            throw new IllegalArgumentException("잔여 시간은 0 이상이어야 합니다.");
        }
        this.remainingMinutes = remainingMinutes;
    }

    /** 이용권 충전 (분 단위) */
    public void addMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("충전 시간은 1분 이상이어야 합니다.");
        }
        this.remainingMinutes += minutes;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | 잔여 %d시간 %02d분",
                id, name, phone, remainingMinutes / 60, remainingMinutes % 60);
    }
}
