package com.tyfanchz.designpattern.behavioral.proxy;

/**
 * 简单代理，调用者手动创建玩家和代理，并将玩家传入代理中，调用代理的执行方法
 */
public interface SimplePlayer {
    void login(String username, String password);

    void treasure();

    void task();

    void logout();
}
