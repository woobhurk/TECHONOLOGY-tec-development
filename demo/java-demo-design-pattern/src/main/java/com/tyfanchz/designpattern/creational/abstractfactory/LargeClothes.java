package com.tyfanchz.designpattern.creational.abstractfactory;

/**
 * 大号衣服
 */
public abstract class LargeClothes implements Clothes {
    @Override
    public void showSize() {
        System.out.println("宽大的衣服，给胖子");
    }
}
