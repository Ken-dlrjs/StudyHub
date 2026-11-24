package io;

import exception.DataFileException;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV 한 줄을 만들고 해석하는 유틸리티.
 * 필드에 쉼표(,)나 큰따옴표(")가 있으면 큰따옴표로 감싸고, 안의 " 는 "" 로 바꾼다.
 */
public final class CsvUtil {
    private CsvUtil() {
    }

    public static String join(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String[] split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (quoted) {
            throw new DataFileException("닫히지 않은 따옴표가 있습니다.");
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }
}
