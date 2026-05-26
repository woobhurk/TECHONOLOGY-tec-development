package com.tyfanchz.dynamicscript.enginev2.test;

import com.tyfanchz.dynamicscript.enginev2.annotation.ScriptParam;

public interface JsFileRunner {
    String mulMyName(@ScriptParam("name") String name, @ScriptParam("times") Integer times);

    void showMyName(@ScriptParam("name") String name);

    Double evalSin(double a, @ScriptParam("b") double b);
}
