package com.tyfanchz.dynamicscript.enginev2.factory;

/**
 * 实例化可运行接口的工厂
 */
public interface ScriptRunnerFactory {
    /**
     * 通过配置类获取可运行实例，从类中读取配置
     *
     * @param tClass 配置类
     * @param <T> 可运行实例的类型
     * @return 获取到的可运行实例
     */
    <T> T getByClass(Class<T> tClass);

    /**
     * 通过类对应的命名空间获取可运行实例，从对应的文件中读取配置
     *
     * @param tClass 类
     * @param <T> 可运行实例的类型
     * @return 获取到的可运行实例
     */
    <T> T getByNamespace(Class<T> tClass);

    /**
     * 通过命名空间获取获取可运行实例，从对应的文件中读取配置
     *
     * @param namespace 命名空间
     * @param <T> 可运行实例的类型
     * @return 获取到的可运行实例
     */
    <T> T getByNamespace(String namespace);
}
