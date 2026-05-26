package com.tyfanchz.designpattern.behavioral.mediator;

import java.util.Random;

public class MediatorTest {
    private static Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        testMediator();
    }

    private static void testMediator() {
        Mediator mediator = new PhoneMediator();
        Buyer buyer = new PhoneBuyer(mediator);
        Seller seller = new PhoneSeller(mediator);
        StockManager stockManager = new PhoneStockManager(mediator);

        System.out.println("==== 买入");
        buyer.buy(random.nextInt(100) + 10);
        System.out.println("==== 卖出");
        seller.sell(random.nextInt(100) + 10);
        System.out.println("==== 降价销售");
        seller.offSell();
        System.out.println("==== 清理库存");
        stockManager.clear();
    }
}
