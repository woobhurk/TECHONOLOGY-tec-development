package com.tyfanchz.dynamicscript.enginev2.factory;

import com.tyfanchz.dynamicscript.enginev2.config.ScriptConfigFactory;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;

/**
 * 可运行实例工厂的公共实现类
 */
@SuppressWarnings("unchecked")
public abstract class BaseScriptRunnerFactory implements ScriptRunnerFactory {
    @Override
    public <T> T getByClass(Class<T> tClass) {
        ScriptConfig scriptConfig;
        T runnerProxy;

        scriptConfig = ScriptConfigFactory.readByClass(tClass);
        runnerProxy = this.getRunnerProxy(scriptConfig, tClass);

        return runnerProxy;
    }

    @Override
    public <T> T getByNamespace(Class<T> tClass) {
        ScriptConfig scriptConfig;
        T runnerProxy;

        scriptConfig = ScriptConfigFactory.readByNamespace(tClass);
        runnerProxy = this.getRunnerProxy(scriptConfig, tClass);

        return runnerProxy;
    }

    @Override
    public <T> T getByNamespace(String namespace) {
        ScriptConfig scriptConfig;
        Class<T> tClass;
        T runnerProxy;

        try {
            scriptConfig = ScriptConfigFactory.readByNamespace(namespace);
            // 不能简单从参数namespace获取Class名称，
            // 需要从配置文件中定义的namespace来获取Class名称
            tClass = (Class<T>) Class.forName(scriptConfig.getNamespace());
            runnerProxy = this.getRunnerProxy(scriptConfig, tClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return runnerProxy;
    }

    /**
     * 获取可运行实例（代理）
     *
     * @param tClass 类
     * @param <T> 可运行实例的类型
     * @return 获取到的可运行实例的代理
     */
    protected abstract <T> T getRunnerProxy(ScriptConfig scriptConfig, Class<T> tClass);
}
