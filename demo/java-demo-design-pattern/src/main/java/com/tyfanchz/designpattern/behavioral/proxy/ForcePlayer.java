package com.tyfanchz.designpattern.behavioral.proxy;

/**
 * 强制代理，如果调用者没有使用玩家指定的代理则无法运行
 */
public interface ForcePlayer {
    ForcePlayer getProxy();

    void login();

    void killingBoss();

    void upgrade();

    void logout();
}
