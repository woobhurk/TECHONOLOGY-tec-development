package com.tyfanchz.designpattern.creational.factory;

public class KillerCreatorImpl implements KillerCreator {
    @Override
    public <T extends Killer> T create(Class<T> killerClass) {
        T killer = null;

        try {
            killer = killerClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return killer;
    }
}
