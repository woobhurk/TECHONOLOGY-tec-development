package com.tyfanchz.designpattern.behavioral.proxy;

public class ForcePrimaryPlayer implements ForcePlayer {
    // 没有实际意义，只是用来判断是否为使用了指定的代理
    private ForcePlayer player = null;
    private String name;

    public ForcePrimaryPlayer(String name) {
        this.name = name;
    }

    @Override
    public ForcePlayer getProxy() {
        this.player = new ForceProxy(this);

        return this.player;
    }

    @Override
    public void login() {
        if (this.isCorrectProxied()) {
            System.out.println(this.name + "随机登录了~");
        } else {
            System.out.println("没有代练我就不玩了！");
        }
    }

    @Override
    public void killingBoss() {
        if (this.isCorrectProxied()) {
            System.out.println(this.name + "杀死了BOSS！！");
        } else {
            System.out.println("没有代练我就不玩了！");
        }
    }

    @Override
    public void upgrade() {
        if (this.isCorrectProxied()) {
            System.out.println(this.name + "：哦豁！升级了！");
        } else {
            System.out.println("没有代练我就不玩了！");
        }
    }

    @Override
    public void logout() {
        if (this.isCorrectProxied()) {
            System.out.println(this.name + "：不玩了！");
        } else {
            System.out.println("没有代练我就不玩了！");
        }
    }

    /**
     * 用于判断是否使用了指定的代理
     *
     * @return 是否使用了指定的代理
     */
    private boolean isCorrectProxied() {
        return (this.player != null);
    }
}
