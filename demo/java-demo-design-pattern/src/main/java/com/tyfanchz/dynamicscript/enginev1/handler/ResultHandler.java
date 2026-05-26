package com.tyfanchz.dynamicscript.enginev1.handler;

import java.lang.reflect.Method;

/**
 * 返回值处理类
 */
public interface ResultHandler {
    /**
     * 将脚本返回值包装成方法可以返回的类型
     * @param method 方法
     * @param result 返回值
     * @return 包装后的返回值
     */
    Object wrapResult(Method method, Object result);
}
