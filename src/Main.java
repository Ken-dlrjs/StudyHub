import service.MemberService;
import service.SeatService;
import ui.ConsoleMenu;

import java.util.Scanner;

/**
 * StudyHub - 스터디 카페 좌석 예약 관리 시스템
 * 프로그램 진입점: 서비스 객체를 만들어 콘솔 메뉴에 연결한다.
 */
public class Main {
    public static void main(String[] args) {
        MemberService memberService = new MemberService();
        SeatService seatService = new SeatService();

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleMenu(memberService, seatService, scanner).run();
        }
    }
}
