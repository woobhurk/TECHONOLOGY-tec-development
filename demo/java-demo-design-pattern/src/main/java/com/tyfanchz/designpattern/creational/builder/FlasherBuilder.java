package com.tyfanchz.designpattern.creational.builder;

import java.util.List;

public abstract class FlasherBuilder {
    private List<FlashStep> flashStepList;

    public void withSteps(List<FlashStep> flashStepList) {
        this.flashStepList = flashStepList;
    }

    public abstract PhoneFlasher buildPhoneFlasher();

    public List<FlashStep> getFlashStepList() {
        return this.flashStepList;
    }

    public void setFlashStepList(
        List<FlashStep> flashStepList) {
        this.flashStepList = flashStepList;
    }
}
