package model;

import exception.DataFileException;
import io.CsvConvertible;

/**
 * 회원 정보. 모든 필드는 private이며, setter에서 값을 검증한다.
 * 잔여 이용 시간은 '분' 단위로 저장한다. (퇴실 시 실제 이용 시간을 정확히 차감하기 위함)
 */
public class Member implements CsvConvertible {
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

    /** 이름 검증. 입력 단계(UI)와 setter에서 모두 사용한다. */
    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
    }

    /** 전화번호 검증: 숫자와 하이픈만 허용하며, 숫자는 9~11자리여야 한다. */
    public static void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("전화번호를 입력해주세요.");
        }
        if (!phone.matches("[0-9-]+")) {
            throw new IllegalArgumentException("전화번호는 숫자와 '-'만 입력할 수 있습니다.");
        }
        int digits = phone.replace("-", "").length();
        if (digits < 9 || digits > 11) {
            throw new IllegalArgumentException("전화번호 자릿수가 올바르지 않습니다. (숫자 9~11자리)");
        }
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        validatePhone(phone);
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

    // ---------------- CSV 변환 ----------------

    @Override
    public String[] toCsvFields() {
        return new String[]{id, name, phone, String.valueOf(remainingMinutes)};
    }

    /** CSV 필드(id,name,phone,remainingMinutes)로 회원을 만든다. 형식이 틀리면 예외 */
    public static Member fromCsv(String[] f) {
        if (f.length != 4) {
            throw new DataFileException("회원 필드 수가 4개가 아닙니다. (" + f.length + "개)");
        }
        try {
            return new Member(f[0].trim(), f[1], f[2].trim(), Integer.parseInt(f[3].trim()));
        } catch (NumberFormatException e) {
            throw new DataFileException("잔여 시간이 숫자가 아닙니다: " + f[3]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        return id.equals(((Member) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | 잔여 %d시간 %02d분",
                id, name, phone, remainingMinutes / 60, remainingMinutes % 60);
    }
}
