package com.tyfanchz.designpattern.behavioral.proxy;

public class DynamicPrimaryPlayer implements DynamicPlayer {
    @Override
    public void play() {
        System.out.println("我就玩玩不说话~");
    }
}
