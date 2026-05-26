package com.tyfanchz.designpattern.creational.abstractfactory;

public class WhiteClothesFactory implements ClothesFactory {
    @Override
    public Clothes createLargeClothes() {
        return new LargeWhiteClothes();
    }

    @Override
    public Clothes createMediumClothes() {
        return new MediumWhiteClothes();
    }

    @Override
    public Clothes createSmallClothes() {
        return new SmallWhiteClothes();
    }
}
