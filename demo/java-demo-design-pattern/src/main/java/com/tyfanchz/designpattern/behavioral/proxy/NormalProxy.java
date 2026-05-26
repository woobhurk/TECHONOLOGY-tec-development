package com.tyfanchz.designpattern.behavioral.proxy;

public class NormalProxy implements NormalPlayer {
    private NormalPlayer player;

    public NormalProxy(String name) {
        System.out.println("正在代理" + name);
        // 在代理初始化的时候自动初始化一个玩家，这样就不需要传玩家实例了
        this.player = new NormalPrimaryPlayer(this, name);
    }

    @Override
    public void login(String username, String password) {
        this.player.login(username, password);
    }

    @Override
    public void getTasks() {
        this.player.getTasks();
    }

    @Override
    public void doTasks() {
        this.player.doTasks();
    }

    @Override
    public void logout() {
        this.player.logout();
    }
}
