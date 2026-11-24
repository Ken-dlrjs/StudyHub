package io;

/**
 * CSV 한 줄로 저장할 수 있는 객체가 구현하는 인터페이스.
 * 반대 방향(CSV -> 객체)은 각 클래스의 정적 메소드 fromCsv(String[])로 제공한다.
 */
public interface CsvConvertible {
    /** 이 객체를 CSV 필드 배열로 변환한다. (이스케이프는 CsvUtil이 처리) */
    String[] toCsvFields();
}
