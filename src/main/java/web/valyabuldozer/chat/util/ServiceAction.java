package web.valyabuldozer.chat.util;

public enum ServiceAction {

    LOGIN("login"),
    REGISTER_USER("register-user");

    private String action;

    public String getValue() {
        return action;
    }

    public static ServiceAction getAction(String action) {
        for (ServiceAction enumValue : values()) {
            if (enumValue.getValue() == action) {
                return enumValue;
            }
        }
        return  null;
    }

    ServiceAction(String action) {
        this.action = action;
    }
}
