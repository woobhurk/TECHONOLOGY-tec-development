package com.tyfanchz.designpattern.creational.factory;

import java.util.List;

public interface Killer {
    void killOne(String name);

    default void killMore(List<String> nameList) {
        for (String name : nameList) {
            this.killOne(name);
        }
    }
}
