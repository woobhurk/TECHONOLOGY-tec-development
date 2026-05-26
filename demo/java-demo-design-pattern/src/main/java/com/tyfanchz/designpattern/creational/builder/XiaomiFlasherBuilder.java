package com.tyfanchz.designpattern.creational.builder;

public class XiaomiFlasherBuilder extends FlasherBuilder {
    @Override
    public PhoneFlasher buildPhoneFlasher() {
        PhoneFlasher xiaomiPhoneFlasher = new XiaomiPhoneFlasher();

        xiaomiPhoneFlasher.withSteps(this.getFlashStepList());

        return xiaomiPhoneFlasher;
    }
}
