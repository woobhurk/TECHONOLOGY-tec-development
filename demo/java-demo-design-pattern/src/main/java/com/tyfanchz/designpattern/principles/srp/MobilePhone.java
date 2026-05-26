package com.tyfanchz.designpattern.principles.srp;

public class MobilePhone implements ConnectionManager, DataManager {
    @Override
    public boolean dial(CallRequest callRequest) {
        System.out.println(callRequest.getFromPhone() + " is calling "
            + callRequest.getToPhone());

        return true;
    }

    @Override
    public boolean hangup(CallRequest callRequest) {
        System.out.println(callRequest.getFromPhone() + " ended call with "
            + callRequest.getToPhone());

        return true;
    }

    @Override
    public boolean transfer(CallRequest callRequest) {
        System.out.println("$$$$" + callRequest.getFromPhone() + ": " + callRequest.getMsg());

        return true;
    }
}
