package com.tyfanchz.designpattern.principles.lsp;

public class HpLaptop implements Laptop {
    @Override
    public void powerOn() {
        System.out.println("HP power on.");
    }

    @Override
    public void showLogo() {
        System.out.println("HP");
    }

    @Override
    public void loginSystem() {
        System.out.println("Welcome, HP.");
    }

    @Override
    public void shutdown() {
        System.out.println("Goodbye, HP.");
    }
}
