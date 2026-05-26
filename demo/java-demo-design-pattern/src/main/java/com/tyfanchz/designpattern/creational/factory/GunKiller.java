package com.tyfanchz.designpattern.creational.factory;

public class GunKiller implements Killer {
    @Override
    public void killOne(String name) {
        System.out.println("扫射" + name + "...");
        System.out.println(name + "挂掉了。");
    }
}
