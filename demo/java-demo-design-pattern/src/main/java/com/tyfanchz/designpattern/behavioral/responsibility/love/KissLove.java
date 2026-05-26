package com.tyfanchz.designpattern.behavioral.responsibility.love;

import com.tyfanchz.designpattern.behavioral.responsibility.action.LoveAction;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class KissLove implements Love {
    @Override
    public LoveAction getLoveAction() {
        return LoveAction.KISS;
    }

    @Override
    public void wellIWantToDo() {
        System.out.println("好想亲亲小甜心！");
    }
}
