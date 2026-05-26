package com.tyfanchz.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ComputerProxyHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;

        System.out.println("无论调用什么方法都会触发这个");

        if (Object.class.equals(method.getDeclaringClass())) {
            // 如果执行的方法是Object继承过来的就能直接调用
            result = method.invoke(this, args);
        } else {
            // 是接口特有的方法
            System.out.println(method.getName() + "执行完毕");
            result = null;
        }

        return result;
    }
}
