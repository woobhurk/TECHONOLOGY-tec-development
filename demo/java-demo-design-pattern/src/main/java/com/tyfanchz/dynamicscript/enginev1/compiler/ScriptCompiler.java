package com.tyfanchz.dynamicscript.enginev1.compiler;

/**
 * 脚本编译器
 * 对脚本中进行格式化、参数替换等操作。
 * 编译方式由子类决定。
 */
public interface ScriptCompiler {
    /**
     * 编译脚本
     * @param script 脚本
     * @param args 参数
     * @return 编译后的脚本
     */
    String compile(String script, Object... args);
}
