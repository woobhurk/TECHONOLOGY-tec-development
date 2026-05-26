package com.tyfanchz.designpattern.behavioral.strategy;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class StrategyTest {
    public static void main(String[] args) {
        testStrategy();
    }

    private static void testStrategy() {
        StrategyExecutor executor = new StrategyExecutor(new TreatStrategy());

        executor.execute();
        executor.execute(new HitStrategy());
        executor.execute(new KillStrategy());
    }
}
