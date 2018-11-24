package web.valyabuldozer.chat.util;

public enum MySqlErrorCode {
    DUPLICATE_ROW(1062);

    MySqlErrorCode(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static MySqlErrorCode getByCode(int code) {
        for (MySqlErrorCode enumValue : values()) {
            if (enumValue.code == code) {
                return enumValue;
            }
        }

        return null;
    }
}
