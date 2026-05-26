package com.tyfanchz.dynamicscript.enginev2.compiler;

/**
 * 使用String.format来格式化脚本
 */
public class DefaultScriptFormatter implements ScriptFormatter {
    @Override
    public String format(String script, Object... args) {
        String formattedScript;

        formattedScript = String.format(script, args);

        return formattedScript;
    }
}
