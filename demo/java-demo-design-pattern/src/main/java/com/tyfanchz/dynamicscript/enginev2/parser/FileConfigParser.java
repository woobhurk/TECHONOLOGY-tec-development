package com.tyfanchz.dynamicscript.enginev2.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;
import com.tyfanchz.dynamicscript.utils.JsonUtils;

/**
 * 通过文件解析脚本配置
 *
 * @deprecated 没啥意义，已停止维护
 */
public class FileConfigParser implements ConfigParser {
    // 配置文件名
    private String configFile;
    // 脚本配置
    private ScriptConfig scriptConfig;

    /**
     * 必须传入配置文件名
     *
     * @param configFile 配置文件名
     */
    public FileConfigParser(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public ScriptConfig parseScriptConfig() {
        InputStream inputStream = FileConfigParser.class.getResourceAsStream(
            this.configFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String configFileContent;

        reader.lines().forEach(s -> stringBuilder.append(s).append("\n"));
        configFileContent = stringBuilder.toString();
        this.scriptConfig = JsonUtils.fromJson(configFileContent, ScriptConfig.class);

        return this.scriptConfig;
    }
}
