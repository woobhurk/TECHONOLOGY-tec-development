package com.tyfanchz.designpattern.creational.singleton;

public class SafeLazySingleton {
    private static SafeLazySingleton safeLazySingleton;

    private SafeLazySingleton() {}

    /**
     * 获取单例（安全版本）
     *
     * @return 单例
     */
    public static SafeLazySingleton getInstance() {
        if (safeLazySingleton == null) {
            syncInstantiate();
        }

        return safeLazySingleton;
    }

    public void sayHello() {
        System.out.println("Hello SafeLazySingleton");
    }

    /**
     * 实例化对象
     * 实例化过程单独放在一个同步方法里面，保证只有一个线程进入此方法，
     * 而且方法结束的时候safeLazySingleton已经获取到了初始化后的内存
     */
    private static synchronized void syncInstantiate() {
        if (safeLazySingleton == null) {
            safeLazySingleton = new SafeLazySingleton();
        }
    }
}
