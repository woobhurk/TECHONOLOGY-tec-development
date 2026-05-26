package com.tyfanchz.designpattern.principles.lsp;

public class AsuaLaptop implements Laptop {
    @Override
    public void powerOn() {
        System.out.println("Asua power on.");
    }

    @Override
    public void showLogo() {
        System.out.println("ASUA");
    }

    @Override
    public void loginSystem() {
        System.out.println("Welcome to Windows: Asua.");
    }

    @Override
    public void shutdown() {
        System.out.println("Goodbye Asua.");
    }
}
