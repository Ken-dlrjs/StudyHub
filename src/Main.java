import exception.DataFileException;
import io.FileManager;
import service.MemberService;
import service.ReservationService;
import service.SeatService;
import ui.ConsoleMenu;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * StudyHub - 스터디 카페 좌석 예약 관리 시스템 (운영자용)
 * 프로그램 진입점: 서비스 객체를 만들고, 파일에서 데이터를 불러온 뒤 콘솔 메뉴를 실행한다.
 * 데이터 폴더는 실행 위치 기준 data/ 이며, 실행 인자로 다른 폴더를 지정할 수 있다.
 */
public class Main {
    public static void main(String[] args) {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");

        MemberService memberService = new MemberService();
        SeatService seatService = new SeatService();
        ReservationService reservationService = new ReservationService();
        FileManager fileManager = new FileManager(dataDir);

        System.out.println("데이터 폴더: " + dataDir.toAbsolutePath());
        fileManager.loadAll(memberService, seatService, reservationService)
                .forEach(System.out::println);

        int noShows = reservationService.processNoShows().size();
        if (noShows > 0) {
            System.out.println("노쇼 처리: 입실하지 않고 시간이 지난 예약 " + noShows + "건");
            try {
                fileManager.saveAll(memberService, reservationService);
            } catch (DataFileException e) {
                System.out.println("  ! 저장 오류: " + e.getMessage());
            }
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(memberService, seatService, reservationService, fileManager, scanner);
            try {
                menu.run();
            } catch (NoSuchElementException e) {
                // 입력 스트림이 끝난 경우(Ctrl+D, 입력 파일 끝 등): 저장 후 종료
                menu.save();
                System.out.println("\n입력이 종료되어 데이터를 저장하고 프로그램을 마칩니다.");
            }
        }
    }
}
