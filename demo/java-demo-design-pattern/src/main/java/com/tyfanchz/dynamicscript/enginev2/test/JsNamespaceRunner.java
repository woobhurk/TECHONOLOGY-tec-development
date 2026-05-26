package com.tyfanchz.dynamicscript.enginev2.test;

import com.tyfanchz.dynamicscript.enginev2.annotation.ScriptParam;

public interface JsNamespaceRunner {
    void showHello();

    Integer evalPlus(Integer a, Integer b);

    Double evalPow(@ScriptParam("a") Integer a, @ScriptParam("b") Integer b);

    Double evalSin(double a, @ScriptParam("b") double b);
}
