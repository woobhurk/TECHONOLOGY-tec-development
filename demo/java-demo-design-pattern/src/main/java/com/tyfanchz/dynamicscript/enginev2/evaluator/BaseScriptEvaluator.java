package com.tyfanchz.dynamicscript.enginev2.evaluator;

import java.lang.reflect.Method;

/**
 * 脚本求值器公共类
 * 定义脚本求值的几个步骤：编译、执行
 */
public abstract class BaseScriptEvaluator implements ScriptEvaluator {
    /**
     * 处理脚本
     * 处理过程包括格式化和编译
     *
     * @param method 要处理的方法
     * @param args 参数
     * @return 处理后的脚本语句
     */
    protected abstract String processScript(Method method, Object... args);

    /**
     * 执行脚本
     *
     * @param script 要执行的脚本
     * @return 执行结果
     * @throws Exception 错误
     */
    protected abstract Object executeScript(String script) throws Exception;
}
