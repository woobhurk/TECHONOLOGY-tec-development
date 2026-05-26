package com.tyfanchz.designpattern.behavioral.mediator;

public class PhoneStockManager extends Colleague implements StockManager {
    private int stockNumber = 0;

    public PhoneStockManager(Mediator mediator) {
        super(mediator);
        super.mediator.setStockManager(this);
    }

    @Override
    public void increase(int number) {
        this.stockNumber += number;
        System.out.println("库存增加" + number + "台，当前库存：" + this.stockNumber);
    }

    @Override
    public void decrease(int number) {
        this.stockNumber -= number;
        System.out.println("库存减少" + number + "台，当前库存：" + this.stockNumber);
    }

    @Override
    public void clear() {
        System.out.println("清理库存，剩余" + this.stockNumber + "台手机");
        this.stockNumber = 0;
    }

    @Override
    public int getStockNumber() {
        return this.stockNumber;
    }
}
