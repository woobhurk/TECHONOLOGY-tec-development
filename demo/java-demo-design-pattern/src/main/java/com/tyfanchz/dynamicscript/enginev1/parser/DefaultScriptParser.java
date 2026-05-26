package com.tyfanchz.dynamicscript.enginev1.parser;

import java.lang.reflect.Method;
import com.tyfanchz.dynamicscript.enginev1.compiler.BasicScriptCompiler;
import com.tyfanchz.dynamicscript.enginev1.compiler.MethodScriptCompiler;
import com.tyfanchz.dynamicscript.enginev1.compiler.ScriptCompiler;

/**
 * 默认的脚本解析类
 */
public class DefaultScriptParser implements ScriptParser {
    private ScriptCompiler basicScriptCompiler = new BasicScriptCompiler();
    private ScriptCompiler methodScriptCompiler;

    @Override
    public String format(String script, Object... args) {
        return this.basicScriptCompiler.compile(script, args);
    }

    @Override
    public String compile(Method method, String script, Object... args) {
        String formattedScript;
        String compiledScript;

        this.methodScriptCompiler = new MethodScriptCompiler(method);
        formattedScript = this.basicScriptCompiler.compile(script, args);
        compiledScript = this.methodScriptCompiler.compile(formattedScript, args);

        return compiledScript;
    }
}
