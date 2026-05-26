package com.tyfanchz.dynamicscript.enginev2.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import com.tyfanchz.dynamicscript.enginev2.constants.DefaultScriptConfig;
import com.tyfanchz.dynamicscript.enginev2.model.MethodConfig;
import com.tyfanchz.dynamicscript.enginev2.model.ScriptConfig;
import com.tyfanchz.dynamicscript.utils.JsonUtils;

/**
 * 从命名空间对应的配置文件读取脚本配置
 */
public class NamespaceConfigParser implements ConfigParser {
    // 命名空间
    private String namespace;
    // 脚本配置
    private ScriptConfig scriptConfig;

    /**
     * 必须传入命名空间
     *
     * @param namespace 命名空间
     */
    public NamespaceConfigParser(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 必须传入命名空间对应的类
     *
     * @param tClass 类
     */
    public NamespaceConfigParser(Class<?> tClass) {
        this(tClass.getName());
    }

    @Override
    public ScriptConfig parseScriptConfig() {
        InputStream configFileInputStream;
        String configFileContent;
        Map<String, MethodConfig> methodConfigMap;

        configFileInputStream = this.getConfigFileInputStream();
        configFileContent = this.getConfigFileContent(configFileInputStream);
        this.scriptConfig = this.parseBaseScriptConfig(configFileContent);
        methodConfigMap = this.parseMethodConfigMap();
        this.scriptConfig.setMethods(methodConfigMap);

        return this.scriptConfig;
    }

    /**
     * 获取配置文件输入流，如果文件不存在则抛出异常
     *
     * @return 配置文件输入流
     */
    private InputStream getConfigFileInputStream() {
        String configFilePath;
        InputStream configFileInputStream;

        configFilePath = "/" + this.namespace.replace(".", "/") + ".json";
        configFileInputStream = NamespaceConfigParser.class.getResourceAsStream(
            configFilePath);

        if (configFileInputStream == null) {
            String errorMsg = String.format(
                "No corresponding configuration file found (%s) for namespace `%s`",
                configFilePath, this.namespace);

            throw new RuntimeException(errorMsg);
        }

        return configFileInputStream;
    }

    /**
     * 从输入流中读取脚本配置文件内容
     *
     * @param inputStream 输入流
     * @return 读取到的脚本配置文件内容
     */
    private String getConfigFileContent(InputStream inputStream) {
        BufferedReader reader;
        StringBuilder configFileContentSb;
        String configFileContent;

        reader = new BufferedReader(new InputStreamReader(inputStream));
        configFileContentSb = new StringBuilder();

        reader.lines().forEach(s -> configFileContentSb.append(s).append("\n"));
        configFileContent = configFileContentSb.toString();

        return configFileContent;
    }

    /**
     * 从文件内容中解析脚本配置
     *
     * @param configFileContent 文件内容
     * @return 脚本配置
     */
    private ScriptConfig parseBaseScriptConfig(String configFileContent) {
        ScriptConfig scriptConfig;

        scriptConfig = JsonUtils.fromJson(configFileContent, ScriptConfig.class);

        if (scriptConfig == null) {
            String errorMsg = String.format(
                "Error when parsing configuration file corresponding to namespace `%s`",
                this.namespace
            );

            throw new RuntimeException(errorMsg);
        }

        // 如果配置里面没有命名空间则自动设置
        if (scriptConfig.getNamespace() == null
            || scriptConfig.getNamespace().trim().isEmpty()) {
            scriptConfig.setNamespace(this.namespace);
        }

        // 设置默认引擎
        if (scriptConfig.getEngine() == null
            || scriptConfig.getEngine().trim().isEmpty()) {
            scriptConfig.setEngine(DefaultScriptConfig.DEFAULT_ENGINE);
        }

        return scriptConfig;
    }

    /**
     * 解析方法配置，做一些预处理
     *
     * @return 方法配置Map
     */
    private Map<String, MethodConfig> parseMethodConfigMap() {
        Map<String, MethodConfig> methodConfigMap = this.scriptConfig.getMethods();

        for (Map.Entry<String, MethodConfig> entry : methodConfigMap.entrySet()) {
            MethodConfig methodConfig = entry.getValue();
            String script = methodConfig.getScript();
            String[] scripts = methodConfig.getScripts();
            StringBuilder scriptSb = new StringBuilder(script + "\n");

            // 将未设置引擎的方法自动设置引擎为脚本配置的引擎
            if (methodConfig.getEngine() == null
                || methodConfig.getEngine().trim().isEmpty()) {
                methodConfig.setEngine(this.scriptConfig.getEngine());
            }

            // 把多行的脚本合并到一起
            if (scripts != null) {
                for (String line : scripts) {
                    scriptSb.append(line).append("\n");
                }
            }

            methodConfig.setScript(scriptSb.toString());
        }

        return methodConfigMap;
    }
}
