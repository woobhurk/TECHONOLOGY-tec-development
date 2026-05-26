package com.tyfanchz.designpattern.principles.lsp;

public class Human {
    private String name;
    private Laptop laptop;

    public Human(String name, Laptop laptop) {
        this.name = name;
        this.laptop = laptop;
    }

    public void usingLaptop() {
        System.out.println(this.name + " is using computer.");
        this.laptop.powerOn();
        this.laptop.showLogo();
        this.laptop.loginSystem();
        this.laptop.shutdown();
        System.out.println(this.name + ": Done.");
    }

    public Laptop getLaptop() {
        return this.laptop;
    }

    public void setLaptop(Laptop laptop) {
        this.laptop = laptop;
    }
}
