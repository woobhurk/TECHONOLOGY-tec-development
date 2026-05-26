package com.tyfanchz.dynamicscript.enginev2.parser;

import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;

/**
 * 配置解析类，解析的方式由子类决定
 */
public interface ConfigParser {
    /**
     * 解析配置
     *
     * @return 解析出来的脚本配置
     */
    ScriptConfig parseScriptConfig();
}
