package com.tyfanchz.designpattern.behavioral.proxy;

import java.lang.reflect.Proxy;

public class ProxyTest {
    public static void main(String[] args) {
        System.out.println("---- testSimpleProxy");
        testSimpleProxy();
        System.out.println("---- testNormalProxy");
        testNormalProxy();
        System.out.println("---- testForceProxy");
        testForceProxy();
        System.out.println("---- testForceProxyUnexpectedly1");
        testForceProxyUnexpectedly1();
        System.out.println("---- testForceProxyUnexpectedly2");
        testForceProxyUnexpectedly2();
        System.out.println("---- testDynamicProxy");
        testDynamicProxy();
    }

    private static void testSimpleProxy() {
        // 创建玩家
        SimplePlayer player = new SimplePrimaryPlayer("我");
        // 创建代理，将玩家传入
        SimplePlayer proxy = new SimpleProxy(player);

        // 玩游戏了
        proxy.login("杀千刀", "sha");
        proxy.treasure();
        proxy.task();
        proxy.logout();
    }

    private static void testNormalProxy() {
        // 只需创建代理，玩家由代理自动创建
        NormalProxy proxy = new NormalProxy("我");

        // 玩游戏了
        proxy.login("杀千刀", "sha");
        proxy.getTasks();
        proxy.doTasks();
        proxy.logout();
    }

    private static void testForceProxy() {
        // 创建玩家
        ForcePlayer player = new ForcePrimaryPlayer("我");
        // 通过玩家来获取指定的代理
        ForcePlayer proxy = player.getProxy();

        // 玩游戏了
        proxy.login();
        proxy.killingBoss();
        proxy.upgrade();
        proxy.logout();
    }

    private static void testForceProxyUnexpectedly1() {
        // 不使用代理
        ForcePlayer player = new ForcePrimaryPlayer("我");

        // 玩游戏了（玩不了）
        player.login();
        player.killingBoss();
        player.upgrade();
        player.logout();
    }

    private static void testForceProxyUnexpectedly2() {
        // 创建玩家
        ForcePlayer player = new ForcePrimaryPlayer("我");
        // 创建代理，并传入玩家，即不使用玩家指定的代理
        ForcePlayer proxy = new ForceProxy(player);

        // 玩游戏了（个锤子）
        proxy.login();
        proxy.killingBoss();
        proxy.upgrade();
        proxy.logout();
    }

    private static void testDynamicProxy() {
        // 创建玩家
        DynamicPlayer player = new DynamicPrimaryPlayer();
        // 动态创建代理
        DynamicPlayer instance = (DynamicPlayer) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(),
            new Class[]{DynamicPlayer.class},
            new DynamicPlayerHandler(player));

        instance.play();
    }
}
