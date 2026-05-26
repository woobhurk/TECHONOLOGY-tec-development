package com.tyfanchz.designpattern.creational.singleton;

public class EarlySingleton {
    /**
     * 防止被实例化
     */
    private EarlySingleton() {}

    /**
     * 用以维护单例对象的工厂类，并且要防止外部访问
     */
    private static class EarlySingletonFactory {
        // 类加载的时候就开始实例化
        private static EarlySingleton earlySingleton = new EarlySingleton();
    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static EarlySingleton getInstance() {
        // 类加载的时候就已经实例化了
        return EarlySingletonFactory.earlySingleton;
    }

    public void sayHello() {
        System.out.println("Hello EarlySingleton");
    }
}
