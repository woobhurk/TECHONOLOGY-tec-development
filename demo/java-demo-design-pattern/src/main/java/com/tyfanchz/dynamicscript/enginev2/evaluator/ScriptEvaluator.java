package com.tyfanchz.dynamicscript.enginev2.evaluator;

import java.lang.reflect.Method;

/**
 * 脚本求值器，提供脚本运行的总方法
 * 脚本求值步骤包括：
 * - 编译脚本
 * - 执行脚本
 */
public interface ScriptEvaluator {
    /**
     * 对脚本求值
     *
     * @param method 要运行的方法
     * @param args 参数
     * @return 运行结果
     * @throws Exception 错误
     */
    Object evaluate(Method method, Object... args) throws Exception;
}
