package com.tyfanchz.designpattern.structural.adapter.their;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class HpAddressInfo implements TheirAddressInfo {
    @Override
    public String getProvince() {
        return "HpProvince";
    }

    @Override
    public String getCity() {
        return "HpCity";
    }

    @Override
    public String getDetail() {
        return "HpDetailAddress";
    }
}
