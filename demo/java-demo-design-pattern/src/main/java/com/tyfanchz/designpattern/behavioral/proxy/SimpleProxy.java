package com.tyfanchz.designpattern.behavioral.proxy;

/**
 * 代练
 */
public class SimpleProxy implements SimplePlayer {
    private SimplePlayer player;

    /**
     * 直接传入一个玩家对象
     *
     * @param player 玩家
     */
    public SimpleProxy(SimplePlayer player) {
        System.out.println("正在代理");
        this.player = player;
    }

    /**
     * 后续所有的操作都是借着玩家的号来玩的
     *
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public void login(String username, String password) {
        this.player.login(username, password);
    }

    @Override
    public void treasure() {
        this.player.treasure();
    }

    @Override
    public void task() {
        this.player.task();
    }

    @Override
    public void logout() {
        this.player.logout();
    }
}
