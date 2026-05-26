package com.tyfanchz.designpattern.behavioral.proxy;

public class ForceProxy implements ForcePlayer {
    private ForcePlayer player;

    /**
     * 必须传入要代理的玩家
     *
     * @param player 玩家
     */
    public ForceProxy(ForcePlayer player) {
        System.out.println("开始代理");
        this.player = player;
    }

    @Override
    public ForceProxy getProxy() {
        return this;
    }

    @Override
    public void login() {
        this.player.login();
    }

    @Override
    public void killingBoss() {
        this.player.killingBoss();
    }

    @Override
    public void upgrade() {
        this.player.upgrade();
    }

    @Override
    public void logout() {
        this.player.logout();
    }
}
