package com.tyfanchz.designpattern.creational.abstractfactory;

/**
 * 小号衣服
 */
public abstract class SmallClothes implements Clothes {
    @Override
    public void showSize() {
        System.out.println("小号的衣服，适合小甜心~");
    }
}
