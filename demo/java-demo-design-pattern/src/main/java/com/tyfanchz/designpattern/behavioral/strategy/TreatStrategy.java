package com.tyfanchz.designpattern.behavioral.strategy;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class TreatStrategy implements Strategy {
    @Override
    public void execute() {
        System.out.println("好生招待着...");
    }
}
