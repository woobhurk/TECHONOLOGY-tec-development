package com.tyfanchz.designpattern.creational.builder;

public class OneplusPhoneFlasher extends PhoneFlasher {
    @Override
    protected void buy() {
        System.out.println("一加手机不需要买，可以抢");
    }

    @Override
    protected void secretKey() {
        System.out.println("伟大的一加不需要解锁码");
    }

    @Override
    protected void unlock() {
        System.out.println("解锁中...");
    }

    @Override
    protected void flash() {
        System.out.println("拿到了一个ROM，开始刷机");
    }

    @Override
    protected void enjoy() {
        System.out.println("一加系统向来很好使");
    }
}
