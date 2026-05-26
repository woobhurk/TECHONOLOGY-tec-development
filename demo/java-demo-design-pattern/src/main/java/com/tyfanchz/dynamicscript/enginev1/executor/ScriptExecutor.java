package com.tyfanchz.dynamicscript.enginev1.executor;

import java.lang.reflect.Method;

/**
 * 脚本执行类
 */
public interface ScriptExecutor {
    /**
     * 执行方法对应的脚本
     * @param method 要执行的方法
     * @param args 传入的参数
     * @return 执行结果
     */
    Object execute(Method method, Object... args) throws Exception;
}
