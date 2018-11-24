package web.valyabuldozer.chat.vertx.database;

public class Producers {
    private String tableName;

    public Producers(String tableName) {
        this.tableName = tableName;
    }


    public String logInQuery(String username, String password) {
        return "SELECT * FROM " + this.tableName +  " WHERE username = '" + username + "' AND password = '" + password + "'";
    }

    public String getUsersByUsername(String username) {
        return "SELECT * FROM " + this.tableName + " WHERE username = '" + username + "'";
    }

    public String insertUser(String username, String password) {
        return String.format("INSERT INTO %s (username, password) VALUES ('%s','%s');", this.tableName, username, password);
    }
}
