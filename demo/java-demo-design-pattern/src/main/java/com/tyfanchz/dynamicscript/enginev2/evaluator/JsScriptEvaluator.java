package com.tyfanchz.dynamicscript.enginev2.evaluator;

import java.lang.reflect.Method;
import java.util.Map;
import com.tyfanchz.dynamicscript.enginev2.compiler.DefaultScriptProcessor;
import com.tyfanchz.dynamicscript.enginev2.compiler.ScriptProcessor;
import com.tyfanchz.dynamicscript.enginev2.executor.JsScriptExecutor;
import com.tyfanchz.dynamicscript.enginev2.executor.ScriptExecutor;
import com.tyfanchz.dynamicscript.enginev2.model.MethodConfig;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;

/**
 * JavaScript脚本求值器
 */
public class JsScriptEvaluator extends BaseScriptEvaluator {
    // 脚本处理器
    private ScriptProcessor scriptProcessor;
    // 脚本执行器
    private ScriptExecutor scriptExecutor;
    // 脚本配置
    private ScriptConfig scriptConfig;
    // 执行脚本的引擎名称
    private String engineName;
    // 处理后的脚本
    private String processedScript;

    /**
     * 必须传入脚本配置
     *
     * @param scriptConfig 脚本配置
     */
    public JsScriptEvaluator(ScriptConfig scriptConfig) {
        this.scriptConfig = scriptConfig;
    }

    @Override
    public Object evaluate(Method method, Object... args) throws Exception {
        Object result;

        this.processedScript = this.processScript(method, args);
        result = this.executeScript(this.processedScript);

        return result;
    }

    @Override
    protected String processScript(Method method, Object... args) {
        Map<String, MethodConfig> methodConfigMap = this.scriptConfig.getMethods();
        MethodConfig methodConfig = methodConfigMap.get(method.getName());
        String script;

        if (methodConfig == null) {
            String errorMsg = String.format(
                "Method `%s` in `%s` has no implementation in configuration",
                method.getName(), this.scriptConfig.getNamespace());

            throw new RuntimeException(errorMsg);
        }

        this.engineName = methodConfig.getEngine();
        script = methodConfig.getScript();
        this.scriptProcessor = new DefaultScriptProcessor(method);
        this.processedScript = this.scriptProcessor.process(script, args);

        return this.processedScript;
    }

    @Override
    protected Object executeScript(String script) throws Exception {
        Object result;

        this.scriptExecutor = new JsScriptExecutor();
        result = this.scriptExecutor.execute(this.engineName, script);

        return result;
    }
}
