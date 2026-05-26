package com.tyfanchz.designpattern.creational.abstractfactory;

public class BlackClothesFactory implements ClothesFactory {
    @Override
    public Clothes createLargeClothes() {
        return new LargeBlackClothes();
    }

    @Override
    public Clothes createMediumClothes() {
        return new MediumBlackClothes();
    }

    @Override
    public Clothes createSmallClothes() {
        return new SmallBlackClothes();
    }
}
