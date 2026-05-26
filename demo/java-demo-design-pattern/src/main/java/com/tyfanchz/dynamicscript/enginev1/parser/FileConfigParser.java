package com.tyfanchz.dynamicscript.enginev1.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.tyfanchz.dynamicscript.enginev1.config.ScriptConfigFactory;
import com.tyfanchz.dynamicscript.enginev1.model.ScriptConfig;
import com.tyfanchz.dynamicscript.utils.JsonUtils;

/**
 * 通过文件解析脚本配置
 */
public class FileConfigParser implements ConfigParser {
    private String configFile;
    private ScriptConfig scriptConfig;

    /**
     * 必须指定文件名
     *
     * @param configFile 配置文件名
     */
    public FileConfigParser(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public ScriptConfig parseScriptConfig() {
        InputStream inputStream = ScriptConfigFactory.class.getResourceAsStream(
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
