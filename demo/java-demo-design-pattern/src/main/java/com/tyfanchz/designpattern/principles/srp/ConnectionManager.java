package com.tyfanchz.designpattern.principles.srp;

public interface ConnectionManager {
    boolean dial(CallRequest callRequest);

    boolean hangup(CallRequest callRequest);
}
