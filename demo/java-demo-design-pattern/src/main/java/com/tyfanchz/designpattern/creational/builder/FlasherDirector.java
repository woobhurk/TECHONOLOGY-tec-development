package com.tyfanchz.designpattern.creational.builder;

import java.util.ArrayList;
import java.util.List;

public class FlasherDirector {
    public PhoneFlasher createXiaomiFlasher() {
        FlasherBuilder flasherBuilder = new XiaomiFlasherBuilder();
        List<FlashStep> flashStepList = new ArrayList<>();

        flashStepList.add(FlashStep.BUY);
        flashStepList.add(FlashStep.SECRET_KEY);
        flashStepList.add(FlashStep.UNLOCK);
        flashStepList.add(FlashStep.FLASH);
        flashStepList.add(FlashStep.ENJOY);
        flasherBuilder.withSteps(flashStepList);

        return flasherBuilder.buildPhoneFlasher();
    }

    public PhoneFlasher createOneplusFlasher() {
        FlasherBuilder flasherBuilder = new OneplusFlasherBuilder();
        List<FlashStep> flashStepList = new ArrayList<>();

        flashStepList.add(FlashStep.BUY);
        flashStepList.add(FlashStep.UNLOCK);
        flashStepList.add(FlashStep.FLASH);
        flashStepList.add(FlashStep.ENJOY);
        flasherBuilder.withSteps(flashStepList);

        return flasherBuilder.buildPhoneFlasher();
    }
}
