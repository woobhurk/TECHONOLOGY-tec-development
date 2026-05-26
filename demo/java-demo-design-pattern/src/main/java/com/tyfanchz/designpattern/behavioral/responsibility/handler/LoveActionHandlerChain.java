package com.tyfanchz.designpattern.behavioral.responsibility.handler;

import java.util.Arrays;
import java.util.List;
import com.tyfanchz.designpattern.behavioral.responsibility.action.LoveAction;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class LoveActionHandlerChain {
    private static List<LoveActionHandler> loveActionHandlerList;

    private LoveActionHandlerChain() {}

    static {
        LoveActionHandler[] loveActionHandlers = new LoveActionHandler[] {
            LoveAction.SEE.get(),
            LoveAction.BOW.get(),
            LoveAction.KISS.get(),
            LoveAction.GO.get(),
        };
        loveActionHandlerList = Arrays.asList(loveActionHandlers);
    }

    public static LoveActionHandler getNext(LoveActionHandler loveActionHandler) {
        LoveActionHandler savedLoveActionHandler = null;

        for (int handlerIndex = 0; handlerIndex < loveActionHandlerList.size(); handlerIndex++) {
            LoveActionHandler currentHandler = loveActionHandlerList.get(handlerIndex);
            String currentHandlerName = currentHandler.getClass().getName();
            String loveActionHandlerName = loveActionHandler.getClass().getName();

            if (currentHandlerName.equals(loveActionHandlerName)
                && handlerIndex < loveActionHandlerList.size() - 1) {
                savedLoveActionHandler = loveActionHandlerList.get(handlerIndex + 1);
                break;
            }
        }

        return savedLoveActionHandler;
    }
}
