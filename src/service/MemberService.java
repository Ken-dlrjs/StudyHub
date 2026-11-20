package service;

import exception.DuplicateException;
import exception.NotFoundException;
import model.Member;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 회원 등록·조회·수정·삭제를 담당한다. (HashMap으로 회원번호 -> 회원 관리) */
public class MemberService {
    private final Map<String, Member> members = new HashMap<>();

    /** 새 회원을 등록하고, 자동 부여된 회원 번호가 담긴 Member를 반환한다. */
    public Member register(String name, String phone, int remainingMinutes) {
        if (findByPhone(phone) != null) {
            throw new DuplicateException("이미 등록된 전화번호입니다: " + phone);
        }
        Member member = new Member(nextId(), name, phone, remainingMinutes);
        members.put(member.getId(), member);
        return member;
    }

    /** 파일에서 불러온 회원을 그대로 추가한다. (3단계 CSV 불러오기에서 사용) */
    public void add(Member member) {
        if (members.containsKey(member.getId())) {
            throw new DuplicateException("이미 존재하는 회원 번호입니다: " + member.getId());
        }
        members.put(member.getId(), member);
    }

    public Member find(String id) {
        Member member = members.get(id);
        if (member == null) {
            throw new NotFoundException("존재하지 않는 회원입니다: " + id);
        }
        return member;
    }

    public List<Member> findAll() {
        List<Member> list = new ArrayList<>(members.values());
        list.sort(Comparator.comparing(Member::getId));
        return list;
    }

    /** 이름에 검색어가 포함된 회원을 찾는다. */
    public List<Member> searchByName(String keyword) {
        List<Member> result = new ArrayList<>();
        for (Member m : findAll()) {
            if (m.getName().contains(keyword.trim())) {
                result.add(m);
            }
        }
        return result;
    }

    public void update(String id, String name, String phone) {
        Member member = find(id);
        Member other = findByPhone(phone);
        if (other != null && !other.getId().equals(id)) {
            throw new DuplicateException("이미 다른 회원이 사용 중인 전화번호입니다: " + phone);
        }
        member.setName(name);
        member.setPhone(phone);
    }

    public void charge(String id, int minutes) {
        find(id).addMinutes(minutes);
    }

    public void delete(String id) {
        find(id); // 없으면 NotFoundException
        members.remove(id);
    }

    public int count() {
        return members.size();
    }

    private Member findByPhone(String phone) {
        for (Member m : members.values()) {
            if (m.getPhone().equals(phone)) {
                return m;
            }
        }
        return null;
    }

    /** 현재 가장 큰 번호 + 1 로 새 회원번호를 만든다. (M001, M002, ...) */
    private String nextId() {
        int max = 0;
        for (String id : members.keySet()) {
            try {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // 형식이 다른 번호는 무시
            }
        }
        return String.format("M%03d", max + 1);
    }
}
