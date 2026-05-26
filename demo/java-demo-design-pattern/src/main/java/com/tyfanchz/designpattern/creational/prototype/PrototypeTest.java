package com.tyfanchz.designpattern.creational.prototype;

import java.util.Random;

public class PrototypeTest {
    private static Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        testPrototype();
    }

    private static void testPrototype() {
        MailSender mailSender = new AdMailSender();
        Mail mail = new Mail(new AdMailTemplate());
        Mail clonedMail;

        for (int i = 0; i < 100; i++) {
            clonedMail = (Mail) mail.clone();
            clonedMail.setReceiver(Math.abs(random.nextInt()) + "@qq.com");
            mailSender.send(clonedMail);
        }
    }
}
