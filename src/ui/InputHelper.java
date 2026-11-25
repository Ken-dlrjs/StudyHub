package ui;

import exception.DuplicateException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * 콘솔 입력 전용 유틸 클래스.
 * 잘못된 값이 들어오면 안내 후 올바른 값이 들어올 때까지 다시 입력받는다.
 * 메뉴 번호(readMenu)를 제외한 모든 입력에서 q를 입력하면 InputCancelledException이 발생한다.
 */
public class InputHelper {
    public static final String CANCEL_KEY = "q";

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /** 한 줄을 읽는다. 취소 키워드면 InputCancelledException */
    private String readLine(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.equalsIgnoreCase(CANCEL_KEY)) {
            throw new InputCancelledException();
        }
        return line;
    }

    /** 메뉴 번호 입력. 취소 키워드를 쓰지 않는다. */
    public int readMenu(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  ! %d ~ %d 사이의 숫자를 입력해주세요.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ! 숫자만 입력할 수 있습니다.");
            }
        }
    }

    /** 비어 있지 않은 문자열을 입력받는다. */
    public String readString(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  ! 값을 입력해주세요.");
        }
    }

    /** 빈 입력도 허용하는 문자열 입력. (엔터만 누르면 "" 반환) */
    public String readOptional(String prompt) {
        return readLine(prompt);
    }

    /**
     * 입력 즉시 검증하는 문자열 입력.
     * validator가 예외를 던지면 오류 메시지를 보여주고 같은 항목을 바로 다시 입력받는다.
     */
    public String readValidated(String prompt, Consumer<String> validator) {
        return readValidated(prompt, null, validator);
    }

    /**
     * 기본값이 있는 검증 입력. 엔터만 누르면 defaultValue를 그대로 반환한다.
     * (defaultValue가 null이면 엔터도 검증 대상)
     */
    public String readValidated(String prompt, String defaultValue, Consumer<String> validator) {
        while (true) {
            String line = readLine(prompt);
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                validator.accept(line);
                return line;
            } catch (IllegalArgumentException | DuplicateException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    /** min ~ max 범위의 정수를 입력받는다. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  ! %d ~ %d 사이의 숫자를 입력해주세요.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ! 숫자만 입력할 수 있습니다.");
            }
        }
    }

    /**
     * 날짜(yyyy-MM-dd)를 입력받는다. 엔터만 누르면 defaultDate를 반환한다.
     * minDate가 null이 아니면 그보다 이전 날짜는 다시 입력받는다.
     */
    public LocalDate readDate(String prompt, LocalDate defaultDate, LocalDate minDate) {
        while (true) {
            String line = readLine(prompt);
            if (line.isEmpty()) {
                return defaultDate;
            }
            try {
                LocalDate date = LocalDate.parse(line);
                if (minDate != null && date.isBefore(minDate)) {
                    System.out.println("  ! " + minDate + " 이전 날짜는 선택할 수 없습니다.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("  ! 날짜 형식이 올바르지 않습니다. (예: 2026-11-25)");
            }
        }
    }

    /** y/n 확인 질문. */
    public boolean confirm(String prompt) {
        while (true) {
            String line = readLine(prompt + " (y/n): ").toLowerCase();
            if (line.equals("y")) return true;
            if (line.equals("n")) return false;
            System.out.println("  ! y 또는 n을 입력해주세요.");
        }
    }
}