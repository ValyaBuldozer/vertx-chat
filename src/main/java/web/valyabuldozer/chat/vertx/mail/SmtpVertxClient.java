package web.valyabuldozer.chat.vertx.mail;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;

public class SmtpVertxClient extends AbstractVerticle {

    @Override
    public void start() {
        MailConfig config = new MailConfig()
            .setHostname("smtp.mail.ru")
            .setPort(465)
            .setLogin(LoginOption.REQUIRED)
            .setSsl(true)
            .setUsername("chel12331@mail.ru")
            .setPassword("12345qweasd");

        MailClient mailClient = MailClient.createShared(vertx, config);

        MailMessage email = new MailMessage()
                .setFrom("Vertx chat User <chel12331@mail.ru>")
                .setTo("kiselek44@yandex.ru")
                .setSubject("Test email with HTML")
                .setText("this is a message")
                .setHtml("<p>REGISTRATION MESSAGE</p>");

        mailClient.sendMail(email, result -> {
            if (result.succeeded()) {
                System.out.println(result.result());
            } else {
                System.out.println("got exception");
                result.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SmtpVertxClient.class.getName());
    }
}
