package com.tyfanchz.dynamicscript.enginev1.compiler;

import java.text.MessageFormat;

/**
 * 使用MessageFormat来格式化
 */
public class MessageScriptCompiler implements ScriptCompiler {
    @Override
    public String compile(String script, Object... args) {
        String compiledScript;

        compiledScript = MessageFormat.format(script, args);

        return compiledScript;
    }
}
