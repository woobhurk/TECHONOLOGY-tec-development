package com.tyfanchz.dynamicscript.enginev2.compiler;

/**
 * 脚本处理器
 * 用于综合处理脚本的格式化和编译
 */
public interface ScriptProcessor extends ScriptFormatter, ScriptCompiler {
    /**
     * 处理脚本，格式化+编译
     *
     * @param script 脚本
     * @param args 参数
     * @return 处理后的脚本
     */
    default String process(String script, Object... args) {
        String formattedScript;
        String processedScript;

        formattedScript = this.format(script, args);
        processedScript = this.compile(formattedScript, args);

        return processedScript;
    }
}
