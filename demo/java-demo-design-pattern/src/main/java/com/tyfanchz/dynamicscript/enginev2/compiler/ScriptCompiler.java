package com.tyfanchz.dynamicscript.enginev2.compiler;

/**
 * 脚本编译器
 * 对脚本进行参数替换等操作
 */
public interface ScriptCompiler {
    /**
     * 编译脚本
     *
     * @param script 脚本
     * @param args 参数
     * @return 编译后的脚本
     */
    String compile(String script, Object... args);
}
