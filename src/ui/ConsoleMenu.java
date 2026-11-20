package ui;

import exception.StudyHubException;
import model.FocusSeat;
import model.Member;
import model.NormalSeat;
import model.Reservation;
import model.ReservationStatus;
import model.Seat;
import model.StudyRoom;
import service.MemberService;
import service.ReservationService;
import service.SeatService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 콘솔 화면 입출력만 담당한다. 실제 처리는 서비스 클래스에 맡기고,
 * 서비스에서 던진 예외를 여기서 잡아 사용자에게 메시지로 보여준다.
 */
public class ConsoleMenu {
    private final MemberService memberService;
    private final SeatService seatService;
    private final ReservationService reservationService;
    private final InputHelper input;

    public ConsoleMenu(MemberService memberService, SeatService seatService,
                       ReservationService reservationService, Scanner scanner) {
        this.memberService = memberService;
        this.seatService = seatService;
        this.reservationService = reservationService;
        this.input = new InputHelper(scanner);
    }

    /** 프로그램 메인 루프 */
    public void run() {
        while (true) {
            System.out.println("\n===== StudyHub 좌석 예약 관리 =====");
            System.out.println(" 1. 회원 관리");
            System.out.println(" 2. 좌석 조회");
            System.out.println(" 3. 예약 관리");
            System.out.println(" 4. 입실 / 퇴실");
            System.out.println(" 0. 종료");
            int choice = input.readInt("메뉴 선택 > ", 0, 4);
            switch (choice) {
                case 1 -> memberMenu();
                case 2 -> seatMenu();
                case 3 -> reservationMenu();
                case 4 -> checkInOutMenu();
                case 0 -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
            }
        }
    }

    /** 하위 메뉴 동작을 실행하고, 발생한 예외를 오류 메시지로 출력한다. */
    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException | StudyHubException e) {
            System.out.println("  ! 오류: " + e.getMessage());
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
            runSafely(() -> {
                switch (choice) {
                    case 1 -> registerMember();
                    case 2 -> listMembers();
                    case 3 -> searchMembers();
                    case 4 -> updateMember();
                    case 5 -> chargeMember();
                    case 6 -> deleteMember();
                }
            });
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
        if (reservationService.hasActiveReservation(id)) {
            throw new IllegalStateException("진행 중인 예약(예약·이용 중)이 있는 회원은 삭제할 수 없습니다. 먼저 예약을 취소하거나 퇴실 처리하세요.");
        }
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
            System.out.println(" 2. 좌석 배치도 (지금)");
            System.out.println(" 3. 좌석 배치도 (날짜·시간 지정)");
            System.out.println(" 4. 좌석 번호로 상세 조회");
            System.out.println(" 0. 이전 메뉴");
            int choice = input.readInt("메뉴 선택 > ", 0, 4);
            if (choice == 0) return;
            runSafely(() -> {
                switch (choice) {
                    case 1 -> listSeats();
                    case 2 -> showLayout(reservationService.now());
                    case 3 -> showLayoutAt();
                    case 4 -> showSeatDetail();
                }
            });
        }
    }

    private void listSeats() {
        System.out.println("총 " + seatService.count() + "석");
        for (Seat seat : seatService.findAll()) {
            System.out.println("  " + seat);
        }
    }

    private void showLayoutAt() {
        LocalDate today = reservationService.now().toLocalDate();
        LocalDate date = input.readDate("날짜 (yyyy-MM-dd, 엔터=오늘) > ", today, null);
        int hour = input.readInt("시각 (" + ReservationService.OPEN_HOUR + "~"
                + (ReservationService.CLOSE_HOUR - 1) + ") > ",
                ReservationService.OPEN_HOUR, ReservationService.CLOSE_HOUR - 1);
        showLayout(date.atTime(hour, 0));
    }

    /** 지정한 시각 기준 배치도. 사용(예약) 중인 좌석은 [XX]로 표시한다. */
    private void showLayout(LocalDateTime time) {
        Set<String> occupied = reservationService.occupiedSeatIds(time);
        System.out.printf("%n%s 기준 좌석 현황 (사용 중 %d석 / 전체 %d석, X 표시 = 사용 중)%n",
                time.toString().replace('T', ' ').substring(0, 16), occupied.size(), seatService.count());
        printRow("일반석 ", seatService.findByType(NormalSeat.class), occupied, 10);
        printRow("집중석 ", seatService.findByType(FocusSeat.class), occupied, 10);
        printRow("스터디룸", seatService.findByType(StudyRoom.class), occupied, 10);
    }

    private void printRow(String title, List<Seat> seats, Set<String> occupied, int perLine) {
        System.out.println("\n[" + title + "]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            String id = seats.get(i).getId();
            String label = occupied.contains(id) ? "X".repeat(id.length()) : id;
            sb.append('[').append(label).append("] ");
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

    // ---------------- 예약 관리 ----------------

    private void reservationMenu() {
        while (true) {
            System.out.println("\n--- 예약 관리 ---");
            System.out.println(" 1. 예약하기");
            System.out.println(" 2. 전체 예약 조회");
            System.out.println(" 3. 회원별 예약 조회");
            System.out.println(" 4. 날짜별 예약 조회");
            System.out.println(" 5. 예약 변경");
            System.out.println(" 6. 예약 취소");
            System.out.println(" 0. 이전 메뉴");
            int choice = input.readInt("메뉴 선택 > ", 0, 6);
            if (choice == 0) return;
            runSafely(() -> {
                switch (choice) {
                    case 1 -> createReservation();
                    case 2 -> printReservations(reservationService.findAll());
                    case 3 -> listByMember();
                    case 4 -> listByDate();
                    case 5 -> changeReservation();
                    case 6 -> cancelReservation();
                }
            });
        }
    }

    private void createReservation() {
        String memberId = input.readString("회원 번호 (예: M001) > ").toUpperCase();
        Member member = memberService.find(memberId);
        System.out.println("  " + member);

        Seat seat = seatService.find(input.readString("좌석 번호 (예: N01, F03, R1) > "));
        System.out.println("  " + seat);

        LocalDate date = readReservationDate(null);
        int startHour = readStartHour(date);
        int hours = readHours(seat, startHour);

        Reservation r = reservationService.reserve(member, seat, date, startHour, hours);
        System.out.println("예약 완료: " + r);
    }

    /** 예약 날짜 입력. 오늘 이전 날짜는 바로 다시 입력받는다. */
    private LocalDate readReservationDate(LocalDate defaultDate) {
        LocalDate today = reservationService.now().toLocalDate();
        String hint = defaultDate == null ? "엔터=오늘" : "엔터=" + defaultDate + " 유지";
        return input.readDate("날짜 (yyyy-MM-dd, " + hint + ") > ",
                defaultDate == null ? today : defaultDate, today);
    }

    /** 시작 시각 입력. 오늘이면 이미 지난 시각은 바로 다시 입력받는다. */
    private int readStartHour(LocalDate date) {
        LocalDateTime now = reservationService.now();
        while (true) {
            int hour = input.readInt("시작 시각 (" + ReservationService.OPEN_HOUR + "~"
                    + (ReservationService.CLOSE_HOUR - 1) + "시) > ",
                    ReservationService.OPEN_HOUR, ReservationService.CLOSE_HOUR - 1);
            if (date.equals(now.toLocalDate()) && hour <= now.getHour()) {
                System.out.println("  ! 이미 지난 시각입니다. " + (now.getHour() + 1) + "시 이후로 입력해주세요.");
                continue;
            }
            return hour;
        }
    }

    /** 이용 시간 입력. 좌석 최대 시간과 운영 종료 시각을 동시에 고려한 범위로 받는다. */
    private int readHours(Seat seat, int startHour) {
        int max = Math.min(seat.getMaxHours(), ReservationService.CLOSE_HOUR - startHour);
        return input.readInt("이용 시간 (1~" + max + "시간, " + seat.getType() + " 최대 "
                + seat.getMaxHours() + "시간) > ", 1, max);
    }

    private void listByMember() {
        String memberId = input.readString("회원 번호 (예: M001) > ").toUpperCase();
        memberService.find(memberId);
        printReservations(reservationService.findByMember(memberId));
    }

    private void listByDate() {
        LocalDate today = reservationService.now().toLocalDate();
        LocalDate date = input.readDate("날짜 (yyyy-MM-dd, 엔터=오늘) > ", today, null);
        printReservations(reservationService.findByDate(date));
    }

    private void printReservations(List<Reservation> list) {
        if (list.isEmpty()) {
            System.out.println("예약 내역이 없습니다.");
            return;
        }
        System.out.println("총 " + list.size() + "건");
        list.forEach(r -> System.out.println("  " + r));
    }

    private void changeReservation() {
        String id = input.readString("변경할 예약 번호 (예: B0001) > ");
        Reservation r = reservationService.find(id);
        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("예약 상태일 때만 변경할 수 있습니다. (현재: " + r.getStatus().getLabel() + ")");
        }
        System.out.println("현재 예약: " + r);

        String seatInput = input.readOptional("새 좌석 번호 (엔터=" + r.getSeat().getId() + " 유지) > ");
        Seat seat = seatInput.isEmpty() ? r.getSeat() : seatService.find(seatInput);
        LocalDate date = readReservationDate(r.getStart().toLocalDate());
        int startHour = readStartHour(date);
        int hours = readHours(seat, startHour);

        reservationService.change(r.getId(), seat, date, startHour, hours);
        System.out.println("변경 완료: " + r);
    }

    private void cancelReservation() {
        List<Reservation> all = reservationService.findAll();
        if (all.isEmpty()) {
            System.out.println("예약 내역이 없습니다.");
            return;
        }
        System.out.println("[전체 예약 목록]");
        printReservations(all);
        if (reservationService.findByStatus(ReservationStatus.RESERVED).isEmpty()) {
            System.out.println("취소할 수 있는 예약(예약 상태)이 없습니다.");
            return;
        }
        String id = input.readString("취소할 예약 번호 (예: B0001) > ");
        Reservation r = reservationService.find(id);
        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("취소할 수 없는 상태입니다: " + r.getStatus().getLabel());
        }
        if (input.confirm("이 예약을 취소할까요? " + r)) {
            reservationService.cancel(r.getId());
            System.out.println("취소 완료.");
        } else {
            System.out.println("취소하지 않았습니다.");
        }
    }

    // ---------------- 입실 / 퇴실 ----------------

    private void checkInOutMenu() {
        while (true) {
            System.out.println("\n--- 입실 / 퇴실 ---");
            System.out.println(" 1. 입실");
            System.out.println(" 2. 퇴실");
            System.out.println(" 3. 현재 이용 중인 목록");
            System.out.println(" 0. 이전 메뉴");
            int choice = input.readInt("메뉴 선택 > ", 0, 3);
            if (choice == 0) return;
            runSafely(() -> {
                switch (choice) {
                    case 1 -> checkIn();
                    case 2 -> checkOut();
                    case 3 -> printReservations(reservationService.findByStatus(ReservationStatus.CHECKED_IN));
                }
            });
        }
    }

    private void checkIn() {
        String id = input.readString("입실할 예약 번호 (예: B0001) > ");
        Reservation r = reservationService.checkIn(id);
        System.out.println("입실 완료: " + r);
    }

    private void checkOut() {
        String id = input.readString("퇴실할 예약 번호 (예: B0001) > ");
        int deducted = reservationService.checkOut(id);
        Reservation r = reservationService.find(id);
        System.out.printf("퇴실 완료: 실제 이용 %d분, 이용권 %d분 차감%n", r.getUsedMinutes(), deducted);
        System.out.println("  " + r.getMember());
        if (deducted < r.getUsedMinutes()) {
            System.out.println("  ! 잔여 이용권이 부족해 " + (r.getUsedMinutes() - deducted) + "분은 차감하지 못했습니다. 충전이 필요합니다.");
        }
    }
}
