package com.tyfanchz.designpattern.behavioral.mediator;

public interface Seller {
    void sell(int number);

    void offSell();

    int getSaleStatus();
}
