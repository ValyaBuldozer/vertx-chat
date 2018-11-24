package web.valyabuldozer.chat.vertx.database;

import com.github.mauricio.async.db.mysql.exceptions.MySQLException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import web.valyabuldozer.chat.util.ServiceAction;
import web.valyabuldozer.chat.util.ServiceErrorCode;

import static web.valyabuldozer.chat.util.MySqlErrorCode.getByCode;

public class DatabaseVerticle extends AbstractVerticle {
    private final String DEFAULT_SEVICE_ADDRESS = "db.service";
    private SQLConnection connection;
    private Producers producers;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DatabaseVerticle());
    }

    @Override
    public void start(Future<Void> startFuture) {
        producers = new Producers("users");
        JsonObject dbConfig = new JsonObject()
                .put("host", "localhost")
                .put("username", "root")
                .put("password", "mysqlroot")
                .put("database", "vertxchat");
        SQLClient dbClient = MySQLClient.createNonShared(vertx, dbConfig);

        dbClient.getConnection(res -> {
            if (res.succeeded()) {
                connection = res.result();
                System.out.println("CONNECTED");

                vertx.eventBus().consumer(DEFAULT_SEVICE_ADDRESS, this::serviceMessageHandler);
                startFuture.complete();
            } else {
                System.err.println(res.cause());
                startFuture.fail(res.cause());
            }
        });
    }

    private void serviceMessageHandler(Message<JsonObject> message) {
        try {
            String action = message.headers().get("action");
            switch (ServiceAction.getAction(action)) {

                case LOGIN:
                    logInUser(message);
                    break;
                case REGISTER_USER:
                    registerUser(message);
                    break;
                default:
                    message.fail(1, "Unknown action");
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
            message.fail(0, e.toString());
        }
    }

    private void logInUser(Message<JsonObject> message) {
        String username = message.body().getString("username");
        String password = message.body().getString("password");

        connection.query(producers.getUsersByUsername(username), queryResult -> {
            if (queryResult.succeeded()) {
                if (queryResult.result().getRows().size() != 0) {
                    if (queryResult.result().getRows().stream().anyMatch(row ->
                            row.getString("password").equals(password))) {
                        message.reply(new JsonObject().put("result", "success"));
                    } else {
                        message.fail(ServiceErrorCode.INVALID_PASSWORD.getValue(), "Invalid password");
                    }
                } else {
                    message.fail(ServiceErrorCode.USER_NOT_FOUND.getValue(), "User not found");
                }
            } else {
                message.fail(ServiceErrorCode.INTERNAL_ERROR.getValue(), queryResult.cause().toString());
            }
        });
    }

    private void registerUser(Message<JsonObject> message) {
        String username = message.body().getString("username");
        String password = message.body().getString("password");

        connection.query(producers.insertUser(username, password), result -> {
            if (result.succeeded()) {
                message.reply(new JsonObject().put("result", "success"));
            } else {
                MySQLException exception = (MySQLException) result.cause();

                switch (getByCode(exception.errorMessage().errorCode())) {

                    case DUPLICATE_ROW:
                        message.fail(ServiceErrorCode.USER_ALREADY_REGISTERED.getValue(), "User already registered");
                        break;
                    default:
                        System.out.println(result.cause());
                        message.fail(ServiceErrorCode.INTERNAL_ERROR.getValue(),"Internal error");
                        break;
                }
                message.fail(ServiceErrorCode.INTERNAL_ERROR.getValue(), result.cause().toString());
            }
        });
    }
}
