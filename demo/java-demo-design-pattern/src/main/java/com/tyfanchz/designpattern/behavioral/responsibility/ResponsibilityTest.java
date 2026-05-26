package com.tyfanchz.designpattern.behavioral.responsibility;

import com.tyfanchz.designpattern.behavioral.responsibility.action.LoveAction;
import com.tyfanchz.designpattern.behavioral.responsibility.handler.LoveActionHandler;
import com.tyfanchz.designpattern.behavioral.responsibility.love.GoLove;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class ResponsibilityTest {
    public static void main(String[] args) {
        testResponsibility();
    }

    private static void testResponsibility() {
        LoveActionHandler loveActionHandler;

        loveActionHandler = LoveAction.SEE.get();
        loveActionHandler.handle(new GoLove());
    }
}
