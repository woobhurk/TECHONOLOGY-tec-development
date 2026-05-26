package com.tyfanchz.designpattern.creational.builder;

public class BuilderTest {
    public static void main(String[] args) {
        testXiaomiFlasher();
        System.out.println("----");
        testOneplusFlasher();
    }

    private static void testXiaomiFlasher() {
        new FlasherDirector().createXiaomiFlasher().startFlash();
    }

    private static void testOneplusFlasher() {
        new FlasherDirector().createOneplusFlasher().startFlash();
    }
}
