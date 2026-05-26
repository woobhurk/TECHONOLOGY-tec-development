package com.tyfanchz.dynamicscript.enginev2.compiler;

import java.lang.reflect.Method;

/**
 * 默认的脚本处理器
 */
public class DefaultScriptProcessor implements ScriptProcessor {
    // 使用默认脚本格式化器
    private ScriptFormatter scriptFormatter;
    // 使用默认的脚本编译器
    private ScriptCompiler scriptCompiler;
    // 要处理的方法
    private Method method;

    /**
     * 必须传入要处理的方法
     * @param method 要处理的方法
     */
    public DefaultScriptProcessor(Method method) {
        this.method = method;
    }

    @Override
    public String format(String script, Object... args) {
        String formattedScript;

        this.scriptFormatter = new DefaultScriptFormatter();
        formattedScript = this.scriptFormatter.format(script, args);

        return formattedScript;
    }

    @Override
    public String compile(String script, Object... args) {
        String compiledScript;

        this.scriptCompiler = new DefaultScriptCompiler(this.method);
        compiledScript = this.scriptCompiler.compile(script, args);

        return compiledScript;
    }
}
