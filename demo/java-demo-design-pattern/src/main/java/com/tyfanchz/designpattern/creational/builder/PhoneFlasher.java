package com.tyfanchz.designpattern.creational.builder;

import java.util.List;

public abstract class PhoneFlasher {
    private List<FlashStep> flashStepList;

    public void startFlash() {
        for (FlashStep flashStep : this.flashStepList) {
            switch (flashStep) {
            case BUY:
                this.buy();
                break;
            case SECRET_KEY:
                this.secretKey();
                break;
            case UNLOCK:
                this.unlock();
                break;
            case FLASH:
                this.flash();
                break;
            case ENJOY:
                this.enjoy();
                break;
            default:
                System.out.println("ERROR");
            }
        }
    }

    protected void withSteps(List<FlashStep> flashStepList) {
        this.flashStepList = flashStepList;
    }

    protected abstract void buy();

    protected abstract void secretKey();

    protected abstract void unlock();

    protected abstract void flash();

    protected abstract void enjoy();
}
