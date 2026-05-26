package com.tyfanchz.designpattern.principles.srp;

public class SrpTest {
    public static void main(String[] args) {
        MobilePhone mobilePhone = new MobilePhone();
        CallRequest callRequest = new CallRequest();
        AnyMan anyMan = new AnyMan();

        callRequest.setFromPhone("134xxxx4321");
        callRequest.setToPhone("156xxxx1234");
        callRequest.setMsg("Hello awesome boy!");
        anyMan.callSomeOne(mobilePhone, callRequest);
    }
}
