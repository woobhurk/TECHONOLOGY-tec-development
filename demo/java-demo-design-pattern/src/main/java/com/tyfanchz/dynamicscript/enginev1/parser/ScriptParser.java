package com.tyfanchz.dynamicscript.enginev1.parser;

import java.lang.reflect.Method;

/**
 * 脚本解析类
 */
public interface ScriptParser {
    /**
     * 格式化脚本
     * @param script 脚本
     * @param args 格式化使用的参数
     * @return 格式化后的脚本
     */
    String format(String script, Object... args);

    /**
     * 编译脚本，根据方法中的参数注解来编译
     * @param method 方法
     * @param script 脚本
     * @param args 编译使用的参数
     * @return 编译后的脚本
     */
    String compile(Method method, String script, Object... args);
}
