package ui;

import java.util.Scanner;

/**
 * 콘솔 입력 전용 유틸 클래스.
 * 잘못된 값이 들어오면 안내 후 올바른 값이 들어올 때까지 다시 입력받는다.
 */
public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /** 비어 있지 않은 문자열을 입력받는다. */
    public String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  ! 값을 입력해주세요.");
        }
    }

    /** min ~ max 범위의 정수를 입력받는다. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.printf("  ! %d ~ %d 사이의 숫자를 입력해주세요.%n", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  ! 숫자만 입력할 수 있습니다.");
            }
        }
    }

    /** y/n 확인 질문. */
    public boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y")) return true;
            if (line.equals("n")) return false;
            System.out.println("  ! y 또는 n을 입력해주세요.");
        }
    }

    /** 엔터를 누를 때까지 대기한다. */
    public void pause() {
        System.out.print("\n(엔터를 누르면 계속합니다)");
        scanner.nextLine();
    }
}
