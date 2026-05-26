package com.tyfanchz.designpattern.behavioral.mediator;

/**
 * 中介者抽象类，用于存储公共的对象
 */
public abstract class Mediator {
    protected Buyer buyer;
    protected Seller seller;
    protected StockManager stockManager;

    /**
     * 用于处理一些需要多方合作的事情
     *
     * @param category 事情所属类别
     * @param action 要做的事情
     * @param params 参数
     */
    public abstract void doSomething(String category, String action, Object... params);

    public Buyer getBuyer() {
        return this.buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Seller getSeller() {
        return this.seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public StockManager getStockManager() {
        return this.stockManager;
    }

    public void setStockManager(StockManager stockManager) {
        this.stockManager = stockManager;
    }
}
