package com.tyfanchz.designpattern.behavioral.mediator;

import java.util.Random;

public class PhoneSeller extends Colleague implements Seller {
    private Random random = new Random(System.currentTimeMillis());

    public PhoneSeller(Mediator mediator) {
        super(mediator);
        super.mediator.setSeller(this);
    }

    @Override
    public void sell(int number) {
        System.out.println("卖手机咯！卖出：" + number);
        // 卖出手机影响到库存，让中介者去做
        super.mediator.doSomething("seller", "sell", number);
    }

    @Override
    public void offSell() {
        System.out.println("卖不出去了，降价销售");
        // 卖出手机影响到库存，让中介者去做
        super.mediator.doSomething("seller", "offSell");
    }

    @Override
    public int getSaleStatus() {
        int saleStatus = this.random.nextInt(100);

        System.out.println("手机销售情况为：" + saleStatus);

        return saleStatus;
    }
}
