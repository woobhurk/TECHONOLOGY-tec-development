package com.tyfanchz.designpattern.creational.singleton;

public class SingletonTest {
    public static void main(String[] args) {
        testEarlySingleton();
        testDangerousLazySingleton();
        testSafeLazySingleton();
    }

    private static void testEarlySingleton() {
        EarlySingleton instance = EarlySingleton.getInstance();

        instance.sayHello();
    }

    private static void testDangerousLazySingleton() {
        DangerousLazySingleton instance = DangerousLazySingleton.getInstance();

        instance.sayHello();
    }

    private static void testSafeLazySingleton() {
        SafeLazySingleton instance = SafeLazySingleton.getInstance();

        instance.sayHello();
    }
}
