package com.tyfanchz.designpattern.creational.factory;

public class KnifeKiller implements Killer {
    @Override
    public void killOne(String name) {
        System.out.println("捅死" + name);
        System.out.println(name + "被捅死了。");
    }
}
