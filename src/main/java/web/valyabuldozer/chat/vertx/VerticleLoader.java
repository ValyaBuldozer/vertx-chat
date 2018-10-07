package web.valyabuldozer.chat.vertx;

import io.vertx.core.Vertx;


public class VerticleLoader {
    public static void load() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Server.class.getName());
    }

    public static void main(String[] args) {
        load();
    }
}
