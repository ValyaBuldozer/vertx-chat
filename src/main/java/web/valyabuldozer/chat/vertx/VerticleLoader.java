package web.valyabuldozer.chat.vertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import web.valyabuldozer.chat.vertx.database.DatabaseVerticle;


public class VerticleLoader {
    public static void load() {

        Vertx vertx = Vertx.vertx();
        Future<String> dbDeployment = Future.future();

        vertx.deployVerticle(DatabaseVerticle.class.getName(), dbDeployment.completer());

        dbDeployment.compose(id -> {

            Future<String> serverDeployment = Future.future();
            vertx.deployVerticle(Server.class.getName(), serverDeployment.completer());
            return serverDeployment;
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println("Verticles deployment success");
            } else {
                System.out.println(ar.cause());
            }
        });
    }

    public static void main(String[] args) {
        load();
    }
}
