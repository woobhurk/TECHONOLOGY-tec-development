package com.tyfanchz.designpattern.structural.adapter.adapter;

import com.tyfanchz.designpattern.structural.adapter.our.OurUserInfo;
import com.tyfanchz.designpattern.structural.adapter.their.TheirAddressInfo;
import com.tyfanchz.designpattern.structural.adapter.their.TheirBasicInfo;
import com.tyfanchz.designpattern.structural.adapter.their.TheirMobileInfo;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class OurUserInfoAdapter implements OurUserInfo {
    private TheirBasicInfo theirBasicInfo;
    private TheirMobileInfo theirMobileInfo;
    private TheirAddressInfo theirAddressInfo;

    public OurUserInfoAdapter(TheirBasicInfo theirBasicInfo, TheirMobileInfo theirMobileInfo,
        TheirAddressInfo theirAddressInfo) {
        this.theirBasicInfo = theirBasicInfo;
        this.theirMobileInfo = theirMobileInfo;
        this.theirAddressInfo = theirAddressInfo;
    }

    @Override
    public String getUserName() {
        return this.theirBasicInfo.getUserName();
    }

    @Override
    public String getUserMobile() {
        return this.theirMobileInfo.getMobileName() + "-"
            + this.theirMobileInfo.getMobileNo();
    }

    @Override
    public String getUserTelephone() {
        return "No telephone";
    }

    @Override
    public String getAddress() {
        return this.theirAddressInfo.getProvince() + "-"
            + this.theirAddressInfo.getCity() + "-"
            + this.theirAddressInfo.getDetail();
    }
}
