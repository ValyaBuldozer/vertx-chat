package web.valyabuldozer.chat.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class Server extends AbstractVerticle {
    private final int DEFAULT_HTTP_PORT = 8080;
    private final String DEFAULT_INBOUND_ADDRESS = "client.to.server";
    private final String DEFAULT_OUTBOUND_ADDRESS = "server.to.client";
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
        router.route("/eventbus/*").handler(handler);

        router.route("/rest/").handler(routerContext -> {
            HttpServerResponse response = routerContext.response();
            response.putHeader("content-type", "text/plain")
                    .end("vertx test");
        });

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
                .addInboundPermitted(new PermittedOptions().setAddress(
                        config().getString("inbound.address", DEFAULT_INBOUND_ADDRESS)))
                .addOutboundPermitted(new PermittedOptions().setAddress(
                        config().getString("outbound.address", DEFAULT_OUTBOUND_ADDRESS)));

        handler.bridge(bridgeOptions, event -> {
            JsonObject rawMessage = event.getRawMessage();

            if (rawMessage == null) {
                System.out.println("Recieved empty message");
                return;
            }

            if (!rawMessage.getString("address").equals(
                    config().getString("inbound.address", DEFAULT_INBOUND_ADDRESS))) {
                System.out.println("Recieved message from unknown address : " + rawMessage.getString("address"));
                return;
            }

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
                default:
                    System.out.println("WARNING : Unknown event type");
                    break;
            }
            event.complete(true);
        });
    }

    private void handlePublishEvent(JsonObject rawMessage) {
        String username = rawMessage.getString("username", "unknown");
        String message = rawMessage.getString("message", "");
        DeliveryOptions options = new DeliveryOptions().addHeader("username", username);

        vertx.eventBus().publish(config().getString("outbound.address", DEFAULT_OUTBOUND_ADDRESS),
                message, options);
    }

    private void handleCloseEvent(JsonObject event) {

    }

    private void handleRegisterEvent(JsonObject event) {

    }
}