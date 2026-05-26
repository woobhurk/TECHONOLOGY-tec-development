package com.tyfanchz.designpattern.creational.abstractfactory;

public class AbstractFactoryTest {
    public static void main(String[] args) {
        testWhiteClothesFactory();
        System.out.println("----");
        testBlackClothesFactory();
    }

    private static void testWhiteClothesFactory() {
        Clothes largeClothes = new WhiteClothesFactory().createLargeClothes();
        Clothes mediumClothes = new WhiteClothesFactory().createMediumClothes();
        Clothes smallClothes = new WhiteClothesFactory().createSmallClothes();

        largeClothes.showSize();
        largeClothes.showColor();
        mediumClothes.showSize();
        mediumClothes.showColor();
        smallClothes.showSize();
        smallClothes.showColor();
    }

    private static void testBlackClothesFactory() {
        Clothes largeClothes = new BlackClothesFactory().createLargeClothes();
        Clothes mediumClothes = new BlackClothesFactory().createMediumClothes();
        Clothes smallClothes = new BlackClothesFactory().createSmallClothes();

        largeClothes.showSize();
        largeClothes.showColor();
        mediumClothes.showSize();
        mediumClothes.showColor();
        smallClothes.showSize();
        smallClothes.showColor();
    }
}
