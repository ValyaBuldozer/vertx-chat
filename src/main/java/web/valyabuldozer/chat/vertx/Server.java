package web.valyabuldozer.chat.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class Server extends AbstractVerticle {
    private final int DEFAULT_HTTP_PORT = 8080;
    private final String DEFAULT_INBOUND_ADDRESS = "client.to.server";
    private final String DEFAULT_OUTBOUND_ADDRESS = "server.to.client";
    private final String DEFAULT_SOCKET_URL = "/eventbus/";
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
                .addInboundPermitted(new PermittedOptions().setAddress(DEFAULT_INBOUND_ADDRESS))
                .addOutboundPermitted(new PermittedOptions().setAddress(DEFAULT_INBOUND_ADDRESS));

        handler.bridge(bridgeOptions, event -> {
            JsonObject rawMessage = event.getRawMessage();

            switch (event.type()) {
                case PUBLISH:
                    handlePublishEvent(rawMessage);
                    break;
                case REGISTER:
                    handleRegisterEvent(rawMessage);
                    break;
                case SOCKET_CLOSED:
                    handleCloseEvent(rawMessage);
                    break;
                case SOCKET_PING:
                    break;
                default:
                    System.out.println("WARNING : Unknown event type");
                    break;
            }
            event.complete(true);
        });
    }

    private void handlePublishEvent(JsonObject rawMessage) {
        if(rawMessage == null) {
            System.out.println("WARNING : empty message at publish handler");
            return;
        }

        String username = rawMessage.getString("username", "unknown");
        String inboundMessage = rawMessage.getString("body", "");
        DeliveryOptions options  = new DeliveryOptions().addHeader("type", "publish");
        JsonObject message = new JsonObject()
                .put("type", "publish")
                .put("message", inboundMessage)
                .put("username", username);

        vertx.eventBus().publish(DEFAULT_INBOUND_ADDRESS,
                message, options);
    }

    private void handleCloseEvent(JsonObject rawMessage) {
        if(rawMessage == null) {
            System.out.println("WARNING : empty message at close handler");
            return;
        }

        String username = rawMessage.getString("username", "unknown");
        JsonObject message = new JsonObject()
                .put("type", "user_disconnect")
                .put("username", username);

        vertx.eventBus().publish(config().getString("outbound.address", DEFAULT_OUTBOUND_ADDRESS),
                message);
    }

    private void handleRegisterEvent(JsonObject rawMessage) {
        if(rawMessage == null) {
            System.out.println("WARNING : empty message at register handler");
            return;
        }

        String username = rawMessage.getString("username", "unknown");
        JsonObject message = new JsonObject()
                .put("type", "user_connected")
                .put("username", username);

        vertx.eventBus().publish(config().getString("outbound.address", DEFAULT_OUTBOUND_ADDRESS),
                message);
    }

    private void configRequestHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        JsonObject config = new JsonObject()
                .put("url", DEFAULT_SOCKET_URL)
                .put("ebaddress", DEFAULT_INBOUND_ADDRESS);
        response.putHeader("content-type", "application/json")
                .end(config.toString());
    }
}
