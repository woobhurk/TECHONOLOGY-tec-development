package com.tyfanchz.dynamicscript.enginev1.handler;

import java.lang.reflect.Method;

/**
 * 默认的返回值处理类
 */
public class DefaultResultHandler implements ResultHandler {
    @Override
    public Object wrapResult(Method method, Object result) {
        Class<?> returnType = method.getReturnType();

        return null;
    }
}
