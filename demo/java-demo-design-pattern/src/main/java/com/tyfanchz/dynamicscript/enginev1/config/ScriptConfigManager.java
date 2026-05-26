package com.tyfanchz.dynamicscript.enginev1.config;

import java.util.HashMap;
import java.util.Map;
import com.tyfanchz.dynamicscript.enginev1.model.MethodConfig;
import com.tyfanchz.dynamicscript.enginev1.model.ScriptConfig;

/**
 * 脚本配置管理类
 * 用于脚本配置的复用，无需每次使用Factory获取一个配置就需要解析
 */
public class ScriptConfigManager {
    private static Map<String, ScriptConfig> scriptConfigMap = new HashMap<>();

    /**
     * 添加脚本配置
     * 如果已存在则将现有的配置和已存的配置合并
     * @param scriptConfig 脚本配置
     */
    public static void add(ScriptConfig scriptConfig) {
        ScriptConfig savedScriptConfig = scriptConfigMap.get(scriptConfig.getNamespace());

        if (savedScriptConfig == null) {
            scriptConfigMap.put(scriptConfig.getNamespace(), scriptConfig);
        } else {
            mergeScriptConfig(scriptConfig, savedScriptConfig);
        }
    }

    /**
     * 添加脚本配置，仅仅添加，不合并
     * @param scriptConfig 脚本配置
     */
    public static void addOnly(ScriptConfig scriptConfig) {
        scriptConfigMap.put(scriptConfig.getNamespace(), scriptConfig);
    }

    /**
     * 获取类对应的脚本配置
     * @param tClass 类
     * @return 获取到的脚本配置
     */
    public static ScriptConfig get(Class<?> tClass) {
        return get(tClass.getName());
    }

    /**
     * 获取命名空间对应脚本配置
     * @param namespace 命名空间
     * @return 获取到的脚本配置
     */
    public static ScriptConfig get(String namespace) {
        return scriptConfigMap.get(namespace);
    }

    /**
     * 删除类对应的脚本配置
     * @param tClass 类
     */
    public static void remove(Class<?> tClass) {
        remove(tClass.getName());
    }

    /**
     * 删除命名空间对应的脚本配置
     * @param namespace 命名空间
     */
    public static void remove(String namespace) {
        scriptConfigMap.remove(namespace);
    }

    /**
     * 判断类对应的脚本配置是否存在
     * @param tClass 类
     * @return 是否存在
     */
    public static boolean exists(Class<?> tClass) {
        return exists(tClass.getName());
    }

    /**
     * 判断命名空间对应的脚本配置是否存在
     * @param namespace 命名空间
     * @return 是否存在
     */
    public static boolean exists(String namespace) {
        return scriptConfigMap.containsKey(namespace);
    }

    /**
     * 合并新加的和已存的脚本配置
     * @param scriptConfig 新加入的脚本配置
     * @param savedScriptConfig 已存的脚本配置
     */
    private static void mergeScriptConfig(ScriptConfig scriptConfig,
        ScriptConfig savedScriptConfig) {
        Map<String, MethodConfig> methodConfigMap = scriptConfig.getMethods();
        Map<String, MethodConfig> savedMethodConfigMap = savedScriptConfig.getMethods();

        // 合并的最小单位为MethodConfig，MethodConfig中的内容不合并
        for (Map.Entry<String, MethodConfig> entry : methodConfigMap.entrySet()) {
            savedMethodConfigMap.put(entry.getKey(), entry.getValue());
        }
    }
}
