package com.tyfanchz.dynamicproxy;

import java.lang.reflect.Proxy;

public class ComputerBuilder {
    public static Computer buildComputer() {
        return (Computer) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(),
            new Class[]{Computer.class},
            new ComputerProxyHandler());
    }
}
