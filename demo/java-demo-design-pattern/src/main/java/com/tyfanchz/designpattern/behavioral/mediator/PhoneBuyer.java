package com.tyfanchz.designpattern.behavioral.mediator;

public class PhoneBuyer extends Colleague implements Buyer {
    public PhoneBuyer(Mediator mediator) {
        super(mediator);
        super.mediator.setBuyer(this);
    }

    @Override
    public void buy(int number) {
        System.out.println("购入手机" + number + "台");
        // 买入手机影响到库存，让中介者去做
        super.mediator.doSomething("buyer", "buy", number);
    }

    @Override
    public void refuseToBuy() {
        System.out.println("终于卖完，不买了！");
    }
}
