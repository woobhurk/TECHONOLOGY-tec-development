package com.tyfanchz.designpattern.creational.builder;

public class OneplusFlasherBuilder extends FlasherBuilder {
    @Override
    public PhoneFlasher buildPhoneFlasher() {
        PhoneFlasher oneplusPhoneFlasher = new OneplusPhoneFlasher();

        oneplusPhoneFlasher.withSteps(this.getFlashStepList());

        return oneplusPhoneFlasher;
    }
}
