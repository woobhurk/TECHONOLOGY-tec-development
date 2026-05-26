package com.tyfanchz.designpattern.creational.factory;

public interface KillerCreator {
    <T extends Killer> T create(Class<T> killerClass);
}
