package com.tyfanchz.designpattern.structural.decorator;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class RawRoom implements Room {
    @Override
    public void show() {
        System.out.println("房间比较大，是复式的，感觉还阔以。");
    }
}
