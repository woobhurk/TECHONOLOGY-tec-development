package com.tyfanchz.designpattern.behavioral.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理实现
 */
public class DynamicPlayerHandler implements InvocationHandler {
    private DynamicPlayer player;

    public DynamicPlayerHandler(DynamicPlayer player) {
        this.player = player;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;

        System.out.println("> 代理正在执行" + method.getName() + "方法");
        result = method.invoke(this.player, args);
        System.out.println("> " + method.getName() + "执行完毕");

        return result;
    }
}
