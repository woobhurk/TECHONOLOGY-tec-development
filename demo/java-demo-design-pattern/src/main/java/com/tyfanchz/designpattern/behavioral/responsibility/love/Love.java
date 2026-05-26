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
public interface Love {
    LoveAction getLoveAction();

    void wellIWantToDo();
}
