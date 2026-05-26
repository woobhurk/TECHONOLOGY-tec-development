package com.tyfanchz.designpattern.behavioral.mediator;

/**
 * 同事抽象类，用于存储公共的中介者
 */
public abstract class Colleague {
    protected Mediator mediator;

    public Colleague(Mediator mediator) {
        this.mediator = mediator;
    }
}
