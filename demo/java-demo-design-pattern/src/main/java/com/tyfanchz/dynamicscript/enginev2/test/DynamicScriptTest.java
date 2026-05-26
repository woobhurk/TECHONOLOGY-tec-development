package com.tyfanchz.dynamicscript.enginev2.test;

import com.tyfanchz.dynamicscript.enginev2.factory.JsScriptRunnerFactory;
import com.tyfanchz.dynamicscript.enginev2.factory.ScriptRunnerFactory;

public class DynamicScriptTest {
    public static void main(String[] args) {
        testJsNamespaceRunner();
        System.out.println("----");
        testJsAnnotationRunner();
        System.out.println("----");
        testJsFileRunner();
    }

    private static void testJsAnnotationRunner() {
        ScriptRunnerFactory scriptRunnerFactory = new JsScriptRunnerFactory();
        JsAnnotationRunner runner;

        System.out.println("$$$$ testJsAnnotationRunner");
        runner = scriptRunnerFactory.getByClass(JsAnnotationRunner.class);
        System.out.println(runner.calMul(23.9, 123.4));
        System.out.println(runner.calDiv(108.0, 24.6));
        runner.formatStr("aaa", "vbbb", "ME");
    }

    private static void testJsNamespaceRunner() {
        ScriptRunnerFactory scriptRunnerFactory = new JsScriptRunnerFactory();
        JsNamespaceRunner runner;

        System.out.println("$$$$ testJsNamespaceRunner");
        runner = scriptRunnerFactory.getByNamespace(JsNamespaceRunner.class);
        runner.showHello();
        System.out.println(runner.evalPlus(4, 5));
        System.out.println(runner.evalPow(2, 10));
        runner.evalSin(30.0, 2.0f);
    }

    private static void testJsFileRunner() {
        ScriptRunnerFactory scriptRunnerFactory = new JsScriptRunnerFactory();
        JsFileRunner runner;

        System.out.println("$$$$ testJsFileRunner");
        runner = scriptRunnerFactory.getByNamespace("com.tyfanch.dynamicscript.JsFileRunner");
        System.out.println(runner.mulMyName("AAA", 10));
        runner.showMyName("BBB");
        System.out.println(runner.evalSin(23.5, 95.2));
    }
}
