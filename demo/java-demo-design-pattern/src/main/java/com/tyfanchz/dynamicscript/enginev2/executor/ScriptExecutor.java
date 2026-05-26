package com.tyfanchz.dynamicscript.enginev2.executor;

/**
 * 脚本执行类
 */
public interface ScriptExecutor {
    /**
     * 执行脚本
     *
     * @param script 脚本
     * @return 执行结果
     * @throws Exception 错误
     */
    Object execute(String script) throws Exception;

    /**
     * 使用指定的引擎执行脚本
     *
     * @param engineName 引擎
     * @param script 脚本
     * @return 执行结果
     * @throws Exception 错误
     */
    Object execute(String engineName, String script) throws Exception;
}
