package web.valyabuldozer.chat.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import web.valyabuldozer.chat.util.ServiceAction;
import web.valyabuldozer.chat.util.ServiceErrorCode;
import web.valyabuldozer.chat.util.TokenGenerator;

import java.sql.Timestamp;
import java.util.*;


public class Server extends AbstractVerticle {
    private final int DEFAULT_HTTP_PORT = 8080;
    private final String DEFAULT_EB_ADDRESS = "client.to.server";
    private final String DEFAULT_USERLIST_ADDRESS = "clients.updates";
    private final String DEFAULT_SOCKET_URL = "/eventbus/";
    private final String DEFAULT_DB_SERVICE_ADDRESS = "db.service";

    private TokenGenerator tokenGenerator = new TokenGenerator();
    private Map<String, String> users = new HashMap<>();
    private SockJSHandler handler;

    @Override
    public void start(Future<Void> startFuture) {

        Integer hostPort = config().getInteger("http.port", DEFAULT_HTTP_PORT);
        Router router = Router.router(vertx);

        handler = SockJSHandler.create(vertx);
        router.route(DEFAULT_SOCKET_URL + "*").handler(handler);

        router.route("/socketconfig").produces("application/json").consumes("application/json").handler(this::configRequestHandler);
        router.route("/authorize").produces("application/json").consumes("application/json").handler(BodyHandler.create());
        router.route("/authorize").produces("application/json").consumes("application/json").handler(this::authorizationHandler);
        router.post("/register").produces("application/json").consumes("application/json").handler(BodyHandler.create());
        router.post("/register").produces("application/json").consumes("application/json").handler(this::registerUserHandler);

        router.route().handler(StaticHandler.create().setCachingEnabled(false));
        createSockHandler();

        vertx.createHttpServer().
                requestHandler(router::accept).listen(hostPort, result -> {
                    if (result.succeeded()) {
                        System.out.print("Server started");
                        createSockHandler();
                        startFuture.complete();
                    } else {
                        System.out.print("Failed to start server");
                        startFuture.fail(result.cause());
                    }
                }
        );
    }

    private void createSockHandler() {
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(DEFAULT_EB_ADDRESS))
                .addOutboundPermitted(new PermittedOptions().setAddress(DEFAULT_EB_ADDRESS));

        handler.bridge(bridgeOptions, event -> {
            JsonObject rawMessage = event.getRawMessage();

            switch (event.type()) {
                case PUBLISH:
                    handlePublishEvent(rawMessage);
                    break;
                case SOCKET_CLOSED:
                    handleCloseEvent(rawMessage);
                    break;
                default:
                    break;
            }
            event.complete(true);
        });
    }


    private void handlePublishEvent(JsonObject rawMessage) {
        if (rawMessage == null) {
            System.out.println("WARNING : empty text at publish handler");
            return;
        }

        JsonObject headers = rawMessage.getJsonObject("headers");
        String body = rawMessage.getString("body");

        String token = headers.getString("token");

        if (!users.containsKey(token)) {
            System.out.println("Recieved message with unknown token");
            return;
        }

        switch (headers.getString("type")) {
            case "message": {
                JsonObject message = new JsonObject()
                        .put("action", "new-message")
                        .put("username", users.get(token))
                        .put("text", body)
                        .put("timestamp", new Timestamp(new Date().getTime()).toString());

                publishEventBusMessage(message);
                break;
            }

            case "disconnect": {
                logOutUser(token);
                break;
            }
            default: {
                System.out.println("Recieved message with unknown type");
                break;
            }
        }
    }

    private void handleCloseEvent(JsonObject rawMessage) {
        if (rawMessage == null) {
            System.out.println("WARNING : empty text at close handler");
            return;
        }

        if (rawMessage.getJsonObject("token") == null) {

        }
    }

    private void configRequestHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        JsonObject config = new JsonObject()
                .put("url", DEFAULT_SOCKET_URL)
                .put("ebaddress", DEFAULT_EB_ADDRESS);
        response.putHeader("content-type", "application/json")
                .end(config.toString());
    }

    private void authorizationHandler(RoutingContext context) {
        HttpServerResponse response = context.response();

        try {
            String json = context.getBodyAsString();
            JsonObject body = new JsonObject(json);

            if (!body.containsKey("username")) {
                response.setStatusCode(400).end("No username specified in body");
                return;
            }

            if (!body.containsKey("password")) {
                response.setStatusCode(400).end("No password header in request body");
                return;
            }

            String username = body.getString("username");
            String password = body.getString("password");
            if (this.users.containsValue(username)) {
                response.setStatusCode(401).end("User with this username already in system");
                return;
            }

            logInUser(username, password, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500).end("Exception handled");
        }
    }

    private void publishEventBusMessage(JsonObject message) {
        vertx.eventBus().publish(DEFAULT_EB_ADDRESS, message);
    }

    private void logInUser(String username, String password, HttpServerResponse response) {
        vertx.eventBus().send(DEFAULT_DB_SERVICE_ADDRESS,
                new JsonObject().put("username", username).put("password", password),
                new DeliveryOptions().addHeader("action", ServiceAction.LOGIN.getValue()),
                reply -> {
                    if (reply.succeeded()) {
                        String token = tokenGenerator.getToken(10);
                        List<String> users = new ArrayList<>(this.users.values());
                        this.users.put(token, username);

                        JsonObject responseBody = new JsonObject()
                                .put("token", token)
                                .put("users", new JsonArray(users));

                        response.setStatusCode(200).end(responseBody.toString());

                        JsonObject updateMessage = new JsonObject()
                                .put("action", "user-login")
                                .put("username", username);
                        publishEventBusMessage(updateMessage);
                    } else {
                        ReplyException exception = (ReplyException) reply.cause();
                        switch (ServiceErrorCode.getError(exception.failureCode())) {

                            case USER_NOT_FOUND:
                                response.setStatusCode(400).end("User with this username not found");
                                break;
                            case INVALID_PASSWORD:
                                response.setStatusCode(400).end("Invalid password");
                                break;
                            default:
                                System.out.println(reply.cause());
                                response.setStatusCode(500).end("Interanl error");
                        }
                    }
                });
    }

    private void registerUserHandler(RoutingContext routingContext) {
        JsonObject message = new JsonObject(routingContext.getBodyAsString());
        String username = message.getString("username");
        String password = message.getString("password");

        if (username == null || password == null) {
            routingContext.response().setStatusCode(400).end("Invalid request format");
            return;
        }

        vertx.eventBus().send(DEFAULT_DB_SERVICE_ADDRESS,
                new JsonObject().put("username", username).put("password", password),
                new DeliveryOptions().addHeader("action", ServiceAction.REGISTER_USER.getValue()),
                reply -> {
            if (reply.succeeded()) {
                routingContext.response().setStatusCode(200).end("User registred");
            } else {
                ReplyException exception = (ReplyException) reply.cause();

                switch (ServiceErrorCode.getError(exception.failureCode())) {

                    case USER_ALREADY_REGISTERED:
                        routingContext.response().setStatusCode(409).end("User already registered");
                        break;
                    case INTERNAL_ERROR:
                        System.out.println(exception.getCause());
                        routingContext.response().setStatusCode(500).end("Internal error");
                        break;
                }
            }
        });
    }

    private void logOutUser(String token) {
        if (!users.containsKey(token)) {
            System.out.println("WARNING : user with token " + token + " not found");
            return;
        }

        String username = users.get(token);
        users.remove(token);
        JsonObject updateMessage = new JsonObject()
                .put("action", "logout-user")
                .put("username", username);
        publishEventBusMessage(updateMessage);
    }
}
