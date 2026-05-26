package com.tyfanchz.designpattern.structural.adapter;

import com.tyfanchz.designpattern.structural.adapter.adapter.OurUserInfoAdapter;
import com.tyfanchz.designpattern.structural.adapter.their.HpAddressInfo;
import com.tyfanchz.designpattern.structural.adapter.their.HpBasicInfo;
import com.tyfanchz.designpattern.structural.adapter.their.HpMobileInfo;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class AdapterTest {
    public static void main(String[] args) {
        testAdapter();
    }

    private static void testAdapter() {
        HpBasicInfo hpBasicInfo = new HpBasicInfo();
        HpMobileInfo hpMobileInfo = new HpMobileInfo();
        HpAddressInfo hpAddressInfo = new HpAddressInfo();
        OurUserInfoAdapter ourUserInfoAdapter;

        ourUserInfoAdapter = new OurUserInfoAdapter(hpBasicInfo, hpMobileInfo, hpAddressInfo);
        System.out.println(ourUserInfoAdapter.getUserName());
        System.out.println(ourUserInfoAdapter.getUserMobile());
        System.out.println(ourUserInfoAdapter.getUserTelephone());
        System.out.println(ourUserInfoAdapter.getAddress());
    }
}
