package com.tyfanchz.designpattern.principles.srp;

import java.io.Serializable;

public class CallRequest implements Serializable {
    private String fromPhone;
    private String toPhone;
    private String msg;

    public String getFromPhone() {
        return this.fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getToPhone() {
        return this.toPhone;
    }

    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CallRequest{" +
            "fromPhone='" + this.fromPhone + '\'' +
            ", toPhone='" + this.toPhone + '\'' +
            ", msg='" + this.msg + '\'' +
            '}';
    }
}
