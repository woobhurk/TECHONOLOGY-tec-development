package com.tyfanchz.dynamicscript.enginev2.handler;

import java.lang.reflect.Method;
import com.tyfanchz.dynamicscript.enginev2.evaluator.JsScriptEvaluator;
import com.tyfanchz.dynamicscript.enginev2.evaluator.ScriptEvaluator;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;

/**
 * JavaScript可运行实例的控制类
 */
public class JsScriptRunnerHandler extends BaseScriptRunnerHandler {
    // 脚本配置
    private ScriptConfig scriptConfig;
    // 脚本运行器
    private ScriptEvaluator scriptEvaluator;

    /**
     * 必须传入脚本配置
     *
     * @param scriptConfig 脚本配置
     */
    public JsScriptRunnerHandler(ScriptConfig scriptConfig) {
        this.scriptConfig = scriptConfig;
    }

    /**
     * 运行方法对应的脚本
     *
     * @param method 要运行的方法
     * @param args 方法参数
     * @return 运行结果
     * @throws Exception 异常
     */
    @Override
    protected Object invokeRunnerMethod(Method method, Object... args) throws Exception {
        Object result;

        this.scriptEvaluator = new JsScriptEvaluator(this.scriptConfig);
        result = this.scriptEvaluator.evaluate(method, args);

        return result;
    }
}
