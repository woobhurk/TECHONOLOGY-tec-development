package com.tyfanchz.dynamicproxy;

public class DynamicProxyTest {
    public static void main(String[] args) {
        testComputerBuilder();
    }

    private static void testComputerBuilder() {
        Computer computer = ComputerBuilder.buildComputer();

        computer.powerOn();
        computer.showLogo();
        computer.loginSystem();
        computer.useSystem();
        computer.shutdown();
    }
}
