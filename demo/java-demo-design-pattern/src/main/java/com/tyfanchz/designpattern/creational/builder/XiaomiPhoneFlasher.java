package com.tyfanchz.designpattern.creational.builder;

public class XiaomiPhoneFlasher extends PhoneFlasher {
    @Override
    public void buy() {
        System.out.println("买了一个小米手机");
    }

    @Override
    public void secretKey() {
        System.out.println("得到了解锁码");
    }

    @Override
    public void unlock() {
        System.out.println("开始解锁！");
    }

    @Override
    public void flash() {
        System.out.println("开始刷机，呼！");
    }

    @Override
    public void enjoy() {
        System.out.println("来看看新系统长啥样");
    }
}
