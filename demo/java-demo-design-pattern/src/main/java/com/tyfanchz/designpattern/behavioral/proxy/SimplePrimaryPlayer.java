package com.tyfanchz.designpattern.behavioral.proxy;

/**
 * 被代理菜鸡玩家
 */
public class SimplePrimaryPlayer implements SimplePlayer {
    private String name;

    public SimplePrimaryPlayer(String name) {
        this.name = name;
    }

    @Override
    public void login(String username, String password) {
        System.out.println(this.name + "正在登录" + username + "的账号，密码为" + password);
        System.out.println("登录成功");
    }

    @Override
    public void treasure() {
        System.out.println(this.name + "获取了所有奖励！");
    }

    @Override
    public void task() {
        System.out.println(this.name + "开始做任务了，呕！");
    }

    @Override
    public void logout() {
        System.out.println(this.name + "：不玩了，哼！");
    }
}
