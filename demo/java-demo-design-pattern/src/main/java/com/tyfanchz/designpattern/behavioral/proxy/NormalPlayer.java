package com.tyfanchz.designpattern.behavioral.proxy;

/**
 * 普通代理，通过创建代理来运行，创建代理的时候代理类会自动创建玩家
 */
public interface NormalPlayer {
    void login(String username, String password);

    void getTasks();

    void doTasks();

    void logout();
}
