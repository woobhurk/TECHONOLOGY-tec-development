package com.tyfanchz.designpattern.creational.builder;

public enum FlashStep {
    BUY("buy", "Buy a new phone"),
    SECRET_KEY("secretKey", "Retrieving secret key..."),
    UNLOCK("unlock", "Unlock fastboot..."),
    FLASH("flash", "Flash ROM..."),
    ENJOY("enjoy", "Enjoy it.");

    private String step;
    private String description;

    FlashStep(String step, String description) {
        this.step = step;
        this.description = description;
    }

    public String getStep() {
        return this.step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
