package web.valyabuldozer.chat.util;

public enum ServiceErrorCode {

    USER_NOT_FOUND(1),
    INVALID_PASSWORD(2),
    USER_ALREADY_REGISTERED(3),
    INTERNAL_ERROR(500);

    private int value;

    public int getValue() {
        return value;
    }

    public static ServiceErrorCode getError(int code) {
        for (ServiceErrorCode serviceError : values()) {
            if (serviceError.getValue() == code) {
                return serviceError;
            }
        }
        return null;
    }

    ServiceErrorCode(int value) {
        this.value = value;
    }
}
