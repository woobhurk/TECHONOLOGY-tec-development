package com.tyfanchz.dynamicscript.enginev1.script;

/**
 * Script执行类工厂
 */
public interface ScriptRunnerFactory {
    /**
     * 通过类获取执行类，从类中读取配置
     * @param tClass 类
     * @param <T> 类的类型
     * @return 获取到的执行类
     */
    <T> T getByClass(Class<T> tClass);

    /**
     * 通过命名空间获取执行类，从对应的文件中读取配置
     * @param tClass 类
     * @param <T> 类的类型
     * @return 获取到的执行类
     */
    <T> T getByNamespace(Class<T> tClass);
}
