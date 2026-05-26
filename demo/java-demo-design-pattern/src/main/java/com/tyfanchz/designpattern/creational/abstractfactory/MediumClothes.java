package com.tyfanchz.designpattern.creational.abstractfactory;

/**
 * 中号衣服
 */
public abstract class MediumClothes implements Clothes {
    @Override
    public void showSize() {
        System.out.println("中等的衣服，适合我这样的人");
    }
}
