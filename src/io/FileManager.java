package io;

import exception.DataFileException;
import exception.StudyHubException;
import model.Member;
import model.Reservation;
import service.MemberService;
import service.ReservationService;
import service.SeatService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원·예약 데이터를 CSV 파일로 저장하고 불러온다.
 *  - data/members.csv      : id,name,phone,remainingMinutes
 *  - data/reservations.csv : id,memberId,seatId,start,end,status,checkInTime,checkOutTime
 * 좌석은 프로그램이 고정 구성으로 생성하므로 저장하지 않는다.
 */
public class FileManager {
    public static final String MEMBER_HEADER = "id,name,phone,remainingMinutes";
    public static final String RESERVATION_HEADER = "id,memberId,seatId,start,end,status,checkInTime,checkOutTime";

    private final Path memberFile;
    private final Path reservationFile;

    public FileManager(Path dataDir) {
        this.memberFile = dataDir.resolve("members.csv");
        this.reservationFile = dataDir.resolve("reservations.csv");
    }

    // ---------------- 불러오기 ----------------

    /**
     * 모든 데이터를 불러온다. 회원을 먼저 읽어야 예약이 회원을 찾아 연결할 수 있다.
     * @return 사용자에게 보여줄 결과 메시지 목록 (경고 포함)
     */
    public List<String> loadAll(MemberService members, SeatService seats, ReservationService reservations) {
        List<String> messages = new ArrayList<>();

        int memberCount = loadLines(memberFile, MEMBER_HEADER, messages, fields -> {
            members.add(Member.fromCsv(fields));
        });
        int reservationCount = loadLines(reservationFile, RESERVATION_HEADER, messages, fields -> {
            reservations.add(Reservation.fromCsv(fields, members, seats));
        });

        messages.add(0, String.format("데이터 불러오기: 회원 %d명, 예약 %d건", memberCount, reservationCount));
        return messages;
    }

    /** CSV 한 줄을 처리하는 동작. (회원/예약마다 다르게 넘겨준다) */
    private interface LineHandler {
        void handle(String[] fields);
    }

    /**
     * 파일을 한 줄씩 읽어 handler에 넘긴다.
     * 파일이 없으면 빈 데이터로 시작하고, 형식이 잘못된 줄은 건너뛰고 경고를 남긴다.
     */
    private int loadLines(Path file, String header, List<String> messages, LineHandler handler) {
        int count = 0;
        int skipped = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 && line.startsWith("﻿")) {
                    line = line.substring(1); // 엑셀 등에서 저장한 BOM 제거
                }
                if (line.isBlank() || (lineNo == 1 && line.equals(header))) {
                    continue;
                }
                try {
                    handler.handle(CsvUtil.split(line));
                    count++;
                } catch (StudyHubException | IllegalArgumentException | IllegalStateException e) {
                    messages.add(String.format("  ! %s %d번째 줄을 건너뜁니다: %s",
                            file.getFileName(), lineNo, e.getMessage()));
                    skipped++;
                }
            }
            if (skipped > 0) {
                backup(file, messages);
            }
        } catch (NoSuchFileException e) {
            messages.add("  - " + file.getFileName() + " 파일이 없어 빈 데이터로 시작합니다.");
        } catch (IOException e) {
            messages.add("  ! " + file.getFileName() + " 파일을 읽지 못했습니다: " + e.getMessage());
        }
        return count;
    }

    /**
     * 건너뛴 줄이 있으면, 다음 저장 때 그 줄이 사라지기 전에 원본을 .bak으로 복사해 둔다.
     * (운영자가 나중에 직접 확인하고 고칠 수 있도록)
     */
    private void backup(Path file, List<String> messages) {
        Path bak = file.resolveSibling(file.getFileName() + ".bak");
        try {
            Files.copy(file, bak, StandardCopyOption.REPLACE_EXISTING);
            messages.add("  - 원본을 " + bak.getFileName() + " 으로 백업했습니다.");
        } catch (IOException e) {
            messages.add("  ! 백업 실패: " + e.getMessage());
        }
    }

    // ---------------- 저장 ----------------

    /** 회원과 예약을 모두 저장한다. 실패하면 DataFileException */
    public void saveAll(MemberService members, ReservationService reservations) {
        List<String> memberLines = new ArrayList<>();
        for (Member m : members.findAll()) {
            memberLines.add(CsvUtil.join(m.toCsvFields()));
        }
        List<String> reservationLines = new ArrayList<>();
        for (Reservation r : reservations.findAll()) {
            reservationLines.add(CsvUtil.join(r.toCsvFields()));
        }
        write(memberFile, MEMBER_HEADER, memberLines);
        write(reservationFile, RESERVATION_HEADER, reservationLines);
    }

    /**
     * 임시 파일에 먼저 쓴 뒤 원래 파일로 교체한다.
     * 저장 도중 오류가 나도 기존 파일이 반쯤 지워진 상태로 남지 않는다.
     */
    private void write(Path file, String header, List<String> lines) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write(header);
                writer.newLine();
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DataFileException(file.getFileName() + " 저장 실패: " + e.getMessage());
        }
    }
}
