package com.tyfanchz.designpattern.behavioral.proxy;

public class NormalPrimaryPlayer implements NormalPlayer {
    private String name;

    /**
     * 必须传入一个代理才行
     *
     * @param player 代理，代理中会有本玩家的实例
     * @param name 玩家名
     */
    public NormalPrimaryPlayer(NormalPlayer player, String name) {
        // 不使用代理则报错
        if (player == null) {
            throw new RuntimeException(name + "：没有代理玩不下去了");
        } else {
            this.name = name;
        }
    }

    @Override
    public void login(String username, String password) {
        System.out.println(this.name + "正在登录" + username + "，使用密码" + password);
    }

    @Override
    public void getTasks() {
        System.out.println(this.name + "领到了很多任务");
    }

    @Override
    public void doTasks() {
        System.out.println(this.name + "开始苦逼地做任务了");
    }

    @Override
    public void logout() {
        System.out.println(this.name + "太菜，不堪其辱，下线了");
    }
}
