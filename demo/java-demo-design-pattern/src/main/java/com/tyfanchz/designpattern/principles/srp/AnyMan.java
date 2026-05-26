package com.tyfanchz.designpattern.principles.srp;

public class AnyMan implements Man {
    @Override
    public void callSomeOne(MobilePhone mobilePhone, CallRequest callRequest) {
        mobilePhone.dial(callRequest);
        mobilePhone.transfer(callRequest);
        mobilePhone.hangup(callRequest);
    }
}
