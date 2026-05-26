package com.tyfanchz.designpattern.behavioral.responsibility.action;

import com.tyfanchz.designpattern.behavioral.responsibility.handler.BowLoveActionHandler;
import com.tyfanchz.designpattern.behavioral.responsibility.handler.GoLoveActionHandler;
import com.tyfanchz.designpattern.behavioral.responsibility.handler.KissLoveActionHandler;
import com.tyfanchz.designpattern.behavioral.responsibility.handler.LoveActionHandler;
import com.tyfanchz.designpattern.behavioral.responsibility.handler.SeeLoveActionHandler;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public enum LoveAction {
    SEE(new SeeLoveActionHandler()),
    BOW(new BowLoveActionHandler()),
    KISS(new KissLoveActionHandler()),
    GO(new GoLoveActionHandler());

    private LoveActionHandler loveActionHandler;

    LoveAction(LoveActionHandler loveActionHandler) {
        this.loveActionHandler = loveActionHandler;
    }

    public LoveActionHandler get() {
        return this.loveActionHandler;
    }
}
