package web.valyabuldozer.chat.vertx;

import com.fasterxml.jackson.databind.util.JSONPObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
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
import web.valyabuldozer.chat.util.TokenGenerator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;


public class Server extends AbstractVerticle {
    private final int DEFAULT_HTTP_PORT = 8080;
    private final String DEFAULT_EB_ADDRESS = "client.to.server";
    private final String DEFAULT_USERLIST_ADDRESS = "clients.updates";
    private final String DEFAULT_SOCKET_URL = "/eventbus/";

    private TokenGenerator tokenGenerator = new TokenGenerator();
    private Map<String, String> users = new HashMap<>();
    private SockJSHandler handler;

    @Override
    public void start() {
        if(deploy()) {
            createSockHandler();
        }
    }

    private boolean deploy() {
        Integer hostPort = config().getInteger("http.port", DEFAULT_HTTP_PORT);
        Router router = Router.router(vertx);

        handler = SockJSHandler.create(vertx);
        router.route(DEFAULT_SOCKET_URL + "*").handler(handler);

        router.route("/socketconfig").handler(this::configRequestHandler);
        router.route("/authorize").handler(BodyHandler.create());
        router.route("/authorize").handler(this::authorizationHandler);

        router.route().handler(StaticHandler.create().setCachingEnabled(false));

        vertx.createHttpServer().
                requestHandler(router::accept)
                .listen(
                        hostPort,
                        result -> {
                            if (result.succeeded()) {
                                System.out.print("Server started");
                            } else {
                                System.out.print("Failed to start server");
                            }
                        }
                );

        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Access to \"CHAT\" at the following address: \nhttp://" + address + ":" + hostPort);
        } catch (UnknownHostException e) {
            System.out.println("Failed to get the local address: [" + e.toString() + "]");
            return false;
        }

        return true;
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
        if(rawMessage == null) {
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
                        .put("message", body)
                        .put("timestamp", new Timestamp(new Date().getTime()).toString());

                vertx.eventBus().publish(DEFAULT_EB_ADDRESS,
                        message);
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
        if(rawMessage == null) {
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

    private void authorizationHandler( RoutingContext context) {
        HttpServerResponse response = context.response();

        try {
            String json = context.getBodyAsString();
            JsonObject body = new JsonObject(json);

            if (!body.containsKey("username")) {
                response.setStatusCode(500).end("No username specified in body");
                return;
            }

            String username = body.getString("username");

            if (this.users.containsValue(username)) {
                //TODO : change status code
                response.setStatusCode(500).end("User with this username already in system");
                return;
            }
            String token = tokenGenerator.getToken(10);
            List<String> users = new ArrayList<>(this.users.values());
            logInUser(token, username);

            JsonObject responseBody = new JsonObject()
                    .put("token", token)
                    .put("users", new JsonArray(users));

            response.setStatusCode(200).end(responseBody.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500).end("Exception handled");
        }
    }

    private void publishEventBusMessage(JsonObject message) {
        vertx.eventBus().publish(DEFAULT_EB_ADDRESS, message);
    }

    private void logInUser(String token, String username) {
        users.put(token, username);

        JsonObject updateMessage = new JsonObject()
                .put("action", "user-login")
                .put("username", username);
        publishEventBusMessage(updateMessage);
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
