package com.tyfanchz.designpattern.behavioral.strategy;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class StrategyExecutor {
    private Strategy strategy;

    public StrategyExecutor() {}

    public StrategyExecutor(Strategy strategy) {
        this.strategy = strategy;
    }

    public void execute() {
        this.execute(this.strategy);
    }

    public void execute(Strategy strategy) {
        strategy.execute();
    }
}
