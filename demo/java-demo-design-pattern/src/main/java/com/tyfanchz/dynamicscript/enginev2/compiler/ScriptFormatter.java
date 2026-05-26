package com.tyfanchz.dynamicscript.enginev2.compiler;

/**
 * 脚本格式化器
 */
public interface ScriptFormatter {
    /**
     * 格式化脚本
     *
     * @param script 脚本
     * @param args 参数
     * @return 格式化后的脚本
     */
    String format(String script, Object... args);
}
