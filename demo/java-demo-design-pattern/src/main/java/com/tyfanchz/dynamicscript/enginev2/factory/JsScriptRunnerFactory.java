package com.tyfanchz.dynamicscript.enginev2.factory;

import java.lang.reflect.Proxy;
import com.tyfanchz.dynamicscript.enginev2.handler.JsScriptRunnerHandler;
import com.tyfanchz.dynamicscript.enginev2.handler.ScriptRunnerHandler;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;

/**
 * 默认的实例化可运行接口的工厂
 */
@SuppressWarnings("unchecked")
public class JsScriptRunnerFactory extends BaseScriptRunnerFactory {
    // 运行控制类
    private ScriptRunnerHandler scriptRunnerHandler;

    @Override
    protected  <T> T getRunnerProxy(ScriptConfig scriptConfig, Class<T> tClass) {
        T runnerProxy;

        this.scriptRunnerHandler = new JsScriptRunnerHandler(scriptConfig);
        runnerProxy = (T) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(),
            new Class[]{tClass},
            this.scriptRunnerHandler);

        return runnerProxy;
    }
}
