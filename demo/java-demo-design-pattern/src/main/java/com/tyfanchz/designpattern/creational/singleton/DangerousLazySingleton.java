package com.tyfanchz.designpattern.creational.singleton;

public class DangerousLazySingleton {
    // 初始化为null
    private static DangerousLazySingleton dangerousLazySingleton = null;

    private DangerousLazySingleton() {}

    /**
     * 获取单例（有风险版本）
     *
     * @return 单例
     */
    public static DangerousLazySingleton getInstance() {
        // A、B线程同时到达
        if (dangerousLazySingleton == null) {
            // A线程进入
            synchronized (DangerousLazySingleton.class) {
                // 此时为空
                if (dangerousLazySingleton == null) {
                    // 实例化，但是这个会分作几部走：
                    // 1、分配内存
                    // 2、初始化实例内存
                    // 3、赋值给dangerousLazySingleton
                    // JVM创建对象的时候不保证2和3按顺序执行，可能是3->2
                    // 因此A可能获取到一个赋值了的实例但是没有初始化
                    dangerousLazySingleton = new DangerousLazySingleton();
                }
            }

            // A退出，B进入时，因为dangerousLazySingleton != null，
            // 因此获取到一个未初始化空间的实例
            // 再次使用的时候就会发生错误
        }

        return dangerousLazySingleton;
    }

    public void sayHello() {
        System.out.println("Hello DangerousLazySingleton");
    }
}
