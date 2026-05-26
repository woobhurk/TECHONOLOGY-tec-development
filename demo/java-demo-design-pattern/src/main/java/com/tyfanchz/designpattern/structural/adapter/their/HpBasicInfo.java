package com.tyfanchz.designpattern.structural.adapter.their;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class HpBasicInfo implements TheirBasicInfo {
    @Override
    public String getUserName() {
        return "HpUser";
    }

    @Override
    public Integer getSex() {
        return 0;
    }

    @Override
    public Integer getAge() {
        return 120;
    }
}
