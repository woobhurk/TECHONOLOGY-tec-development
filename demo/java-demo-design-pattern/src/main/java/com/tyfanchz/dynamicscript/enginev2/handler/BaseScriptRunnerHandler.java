package com.tyfanchz.dynamicscript.enginev2.handler;

import java.lang.reflect.Method;

/**
 * 可运行实例控制类的公共实现
 */
public abstract class BaseScriptRunnerHandler implements ScriptRunnerHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;

        // 判断方法从何处声明，用来区分用户自定义方法和JDK自带方法
        if (Object.class.equals(method.getDeclaringClass())) {
            // 方法是从Object继承过来的
            result = method.invoke(this, args);
        } else {
            // 方法是接口定义的，运行脚本
            result = this.invokeRunnerMethod(method, args);
        }

        return result;
    }

    protected abstract Object invokeRunnerMethod(Method method, Object... args) throws Exception;
}
