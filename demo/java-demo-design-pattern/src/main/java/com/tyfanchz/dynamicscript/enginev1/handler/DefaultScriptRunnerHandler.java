package com.tyfanchz.dynamicscript.enginev1.handler;

import java.lang.reflect.Method;
import com.tyfanchz.dynamicscript.enginev1.executor.DefaultScriptExecutor;
import com.tyfanchz.dynamicscript.enginev1.executor.ScriptExecutor;
import com.tyfanchz.dynamicscript.enginev1.model.ScriptConfig;

/**
 * 默认的运行控制类
 */
public class DefaultScriptRunnerHandler implements ScriptRunnerHandler {
    // 脚本配置
    private ScriptConfig scriptConfig;

    /**
     * 必须传入脚本配置
     * @param scriptConfig 脚本配置
     */
    public DefaultScriptRunnerHandler(ScriptConfig scriptConfig) {
        this.scriptConfig = scriptConfig;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Object result;

        // 判断方法从何处声明，用来区分用户自定义方法和JDK自带方法
        if (Object.class.equals(method.getDeclaringClass())) {
            // 方法是从Object继承过来的
            result = method.invoke(this, args);
        } else {
            // 方法是接口定义的，执行解析函数
            result = this.executeScript(method, args);
        }

        return result;
    }

    /**
     * 执行方法对应的脚本
     * @param method 要执行的方法
     * @param args 方法参数
     * @return 执行结果
     * @throws Exception 异常
     */
    private Object executeScript(Method method, Object... args) throws Exception {
        ScriptExecutor scriptExecutor = new DefaultScriptExecutor(this.scriptConfig);
        Object result;

        result = scriptExecutor.execute(method, args);

        return result;
    }
}
