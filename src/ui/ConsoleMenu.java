package ui;

import exception.DuplicateException;
import exception.NotFoundException;
import model.FocusSeat;
import model.Member;
import model.NormalSeat;
import model.Seat;
import model.StudyRoom;
import service.MemberService;
import service.SeatService;

import java.util.List;
import java.util.Scanner;

/**
 * 콘솔 화면 입출력만 담당한다. 실제 처리는 서비스 클래스에 맡기고,
 * 서비스에서 던진 예외를 여기서 잡아 사용자에게 메시지로 보여준다.
 */
public class ConsoleMenu {
    private final MemberService memberService;
    private final SeatService seatService;
    private final InputHelper input;

    public ConsoleMenu(MemberService memberService, SeatService seatService, Scanner scanner) {
        this.memberService = memberService;
        this.seatService = seatService;
        this.input = new InputHelper(scanner);
    }

    /** 프로그램 메인 루프 */
    public void run() {
        while (true) {
            System.out.println("\n===== StudyHub 좌석 예약 관리 =====");
            System.out.println(" 1. 회원 관리");
            System.out.println(" 2. 좌석 조회");
            System.out.println(" 0. 종료");
            int choice = input.readInt("메뉴 선택 > ", 0, 2);
            switch (choice) {
                case 1 -> memberMenu();
                case 2 -> seatMenu();
                case 0 -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
            }
        }
    }

    // ---------------- 회원 관리 ----------------

    private void memberMenu() {
        while (true) {
            System.out.println("\n--- 회원 관리 ---");
            System.out.println(" 1. 회원 등록");
            System.out.println(" 2. 전체 회원 조회");
            System.out.println(" 3. 이름으로 검색");
            System.out.println(" 4. 회원 정보 수정");
            System.out.println(" 5. 이용권 충전");
            System.out.println(" 6. 회원 삭제");
            System.out.println(" 0. 이전 메뉴");
            int choice = input.readInt("메뉴 선택 > ", 0, 6);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> registerMember();
                    case 2 -> listMembers();
                    case 3 -> searchMembers();
                    case 4 -> updateMember();
                    case 5 -> chargeMember();
                    case 6 -> deleteMember();
                }
            } catch (IllegalArgumentException | NotFoundException | DuplicateException e) {
                System.out.println("  ! 오류: " + e.getMessage());
            }
        }
    }

    private void registerMember() {
        String name = input.readValidated("이름 > ", Member::validateName);
        String phone = readNewPhone("전화번호 (예: 010-1234-5678) > ", null);
        int hours = input.readInt("충전할 이용권 시간(시간 단위, 0~100) > ", 0, 100);
        Member member = memberService.register(name, phone, hours * 60);
        System.out.println("등록 완료: " + member);
    }

    private void listMembers() {
        List<Member> list = memberService.findAll();
        if (list.isEmpty()) {
            System.out.println("등록된 회원이 없습니다.");
            return;
        }
        System.out.println("총 " + list.size() + "명");
        list.forEach(m -> System.out.println("  " + m));
    }

    private void searchMembers() {
        String keyword = input.readString("검색할 이름 > ");
        List<Member> result = memberService.searchByName(keyword);
        if (result.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return;
        }
        result.forEach(m -> System.out.println("  " + m));
    }

    private void updateMember() {
        String id = input.readString("수정할 회원 번호 (예: M001) > ").toUpperCase();
        Member current = memberService.find(id);
        System.out.println("현재 정보: " + current);
        String name = input.readValidated("새 이름 > ", Member::validateName);
        String phone = readNewPhone("새 전화번호 (예: 010-1234-5678) > ", id);
        memberService.update(id, name, phone);
        System.out.println("수정 완료: " + memberService.find(id));
    }

    /** 전화번호를 입력받는 즉시 형식과 중복을 검사한다. 오류면 같은 항목을 다시 입력받는다. */
    private String readNewPhone(String prompt, String excludeId) {
        return input.readValidated(prompt, phone -> {
            Member.validatePhone(phone);
            memberService.checkPhoneAvailable(phone, excludeId);
        });
    }

    private void chargeMember() {
        String id = input.readString("충전할 회원 번호 (예: M001) > ").toUpperCase();
        memberService.find(id);
        int hours = input.readInt("충전할 시간(시간 단위, 1~100) > ", 1, 100);
        memberService.charge(id, hours * 60);
        System.out.println("충전 완료: " + memberService.find(id));
    }

    private void deleteMember() {
        String id = input.readString("삭제할 회원 번호 (예: M001) > ").toUpperCase();
        Member member = memberService.find(id);
        if (input.confirm("정말 삭제할까요? " + member)) {
            memberService.delete(id);
            System.out.println("삭제 완료.");
        } else {
            System.out.println("삭제를 취소했습니다.");
        }
    }

    // ---------------- 좌석 조회 ----------------

    private void seatMenu() {
        while (true) {
            System.out.println("\n--- 좌석 조회 ---");
            System.out.println(" 1. 전체 좌석 목록");
            System.out.println(" 2. 좌석 배치도");
            System.out.println(" 3. 좌석 번호로 상세 조회");
            System.out.println(" 0. 이전 메뉴");
            int choice = input.readInt("메뉴 선택 > ", 0, 3);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> listSeats();
                    case 2 -> showLayout();
                    case 3 -> showSeatDetail();
                }
            } catch (NotFoundException e) {
                System.out.println("  ! 오류: " + e.getMessage());
            }
        }
    }

    private void listSeats() {
        System.out.println("총 " + seatService.count() + "석");
        for (Seat seat : seatService.findAll()) {
            System.out.println("  " + seat);
        }
    }

    /** 좌석 종류별로 번호를 한 줄에 나열한다. (예약 기능이 생기면 사용 중인 좌석은 [XX]로 표시할 예정) */
    private void showLayout() {
        printRow("일반석 ", seatService.findByType(NormalSeat.class), 10);
        printRow("집중석 ", seatService.findByType(FocusSeat.class), 10);
        printRow("스터디룸", seatService.findByType(StudyRoom.class), 10);
    }

    private void printRow(String title, List<Seat> seats, int perLine) {
        System.out.println("\n[" + title + "]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            sb.append('[').append(seats.get(i).getId()).append("] ");
            if ((i + 1) % perLine == 0) {
                sb.append('\n');
            }
        }
        System.out.println(sb.toString().stripTrailing());
    }

    private void showSeatDetail() {
        String id = input.readString("좌석 번호 (예: N01, F03, R1) > ");
        System.out.println(seatService.find(id));
    }
}
