package com.tyfanchz.designpattern.structural.adapter.their;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class HpMobileInfo implements TheirMobileInfo {
    @Override
    public String getMobileName() {
        return "HpMobileName";
    }

    @Override
    public String getMobileNo() {
        return "Hp010-1291844059";
    }
}
