package com.tyfanchz.dynamicscript.enginev1.script;

import java.lang.reflect.Proxy;
import com.tyfanchz.dynamicscript.enginev1.config.ScriptConfigFactory;
import com.tyfanchz.dynamicscript.enginev1.handler.DefaultScriptRunnerHandler;
import com.tyfanchz.dynamicscript.enginev1.model.ScriptConfig;

/**
 * 默认的Script执行类工厂
 */
@SuppressWarnings("unchecked")
public class DefaultScriptRunnerFactory implements ScriptRunnerFactory {
    private ScriptConfig scriptConfig;

    @Override
    public <T> T getByClass(Class<T> tClass) {
        T runnerProxy;

        this.scriptConfig = ScriptConfigFactory.readFromClass(tClass);
        runnerProxy = this.getRunnerProxy(tClass);

        return runnerProxy;
    }

    @Override
    public <T> T getByNamespace(Class<T> tClass) {
        T runnerProxy;

        this.scriptConfig = ScriptConfigFactory.readFromNamespace(tClass);
        runnerProxy = this.getRunnerProxy(tClass);

        return runnerProxy;
    }

    /**
     * 获取执行类的实例（代理）
     * @param tClass 类
     * @param <T> 类的类型
     * @return 获取到的示例（代理）
     */
    private <T> T getRunnerProxy(Class<T> tClass) {
        DefaultScriptRunnerHandler scriptRunnerHandler;
        T runnerProxy;

        scriptRunnerHandler = new DefaultScriptRunnerHandler(this.scriptConfig);
        runnerProxy = (T) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(),
            new Class[]{tClass},
            scriptRunnerHandler);

        return runnerProxy;
    }
}
