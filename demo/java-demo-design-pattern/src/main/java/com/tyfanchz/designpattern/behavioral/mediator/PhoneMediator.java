package com.tyfanchz.designpattern.behavioral.mediator;

/**
 * 手机中介者
 */
public class PhoneMediator extends Mediator {
    @Override
    public void doSomething(String category, String action, Object... params) {
        switch (category) {
        case "buyer":
            this.buyer(action, params);
            break;
        case "seller":
            this.seller(action, params);
            break;
        case "stockManager":
            this.stockManager(action, params);
            break;
        default:
            System.out.println("Unknown category: " + category);
            break;
        }
    }

    private void buyer(String action, Object... params) {
        int number;
        int saleStatus;

        switch (action) {
        case "buy":
            number = (int) params[0];
            saleStatus = this.seller.getSaleStatus();

            if (saleStatus < 60) {
                System.out.println("销售情况极差，只买一半：" + (number / 2));
                super.stockManager.increase(number / 2);
            } else if (saleStatus < 90) {
                System.out.println("销售情况不错，要多少买多少：" + number);
                super.stockManager.increase(number);
            } else {
                System.out.println("销售情况非常好，多多地买：" + (number * 3 / 2));
                super.stockManager.increase(number * 3 / 2);
            }

            break;
        default:
            System.out.println("Unknown action: " + action);
        }
    }

    private void seller(String action, Object... params) {
        int number;

        switch (action) {
        case "sell":
            number = (int) params[0];

            if (super.stockManager.getStockNumber() < number) {
                System.out.println("库存不够了，赶紧加货：" + super.stockManager.getStockNumber()
                     + "/" + number);
                super.stockManager.increase(number);
            }

            super.stockManager.decrease(number);
            break;
        case "offSell":
            super.stockManager.clear();
            super.buyer.refuseToBuy();
            break;
        default:
            System.out.println("Unknown action: " + action);
        }
    }

    private void stockManager(String action, Object... params) {
        switch (action) {
        case "clear":
            super.seller.offSell();
            super.buyer.refuseToBuy();
            break;
        default:
            System.out.println("Unknown action: " + action);
        }
    }
}
