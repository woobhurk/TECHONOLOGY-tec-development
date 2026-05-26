package com.tyfanchz.dynamicscript.enginev2.compiler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.tyfanchz.dynamicscript.enginev2.annotation.ScriptParam;
import com.tyfanchz.dynamicscript.utils.TemplateStringUtils;

/**
 * 默认的脚本编译器
 */
public class DefaultScriptCompiler implements ScriptCompiler {
    // 要编译的方法
    private Method method;

    /**
     * 必须传入要编译的方法
     * @param method 要编译的方法
     */
    public DefaultScriptCompiler(Method method) {
        this.method = method;
    }

    @Override
    public String compile(String script, Object... args) {
        Map<String, Object> paramMap;
        String compiledString;

        paramMap = this.buildParamMap(args);
        compiledString = this.compileByParamMap(script, paramMap);

        return compiledString;
    }

    /**
     * 建立注解中参数名和参数值的映射关系，生成Map
     * @param args 传入的参数
     * @return 建立的映射关系的Map
     */
    private Map<String, Object> buildParamMap(Object... args) {
        Annotation[][] allParamAnnotations = this.method.getParameterAnnotations();
        Map<String, Object> paramMap = new HashMap<>();

        if (allParamAnnotations != null) {
            for (int i = 0; i < allParamAnnotations.length; i++) {
                Annotation[] perParamAnnotations = allParamAnnotations[i];
                String paramName = this.getParamNameInAnnotations(perParamAnnotations);

                if (paramName != null) {
                    paramMap.put(paramName, args[i]);
                }
            }
        }

        return paramMap;
    }

    /**
     * 获取单个参数中ScriptParam注解的值，即参数名
     * @param annotations 单个参数的所有注解
     * @return 如果有注解则返回参数名，如果没有返回null
     */
    private String getParamNameInAnnotations(Annotation[] annotations) {
        String paramName = null;

        for (Annotation annotation : annotations) {
            if (annotation instanceof ScriptParam) {
                String value = ((ScriptParam) annotation).value().trim();
                String name = ((ScriptParam) annotation).name().trim();

                if (value.isEmpty() && name.isEmpty()) {
                    String errorMsg = String.format(
                        "Parameter name error: `%s` and `%s`",
                        value, name
                    );

                    throw new RuntimeException(errorMsg);
                }

                paramName = value.isEmpty() ? name : value;
                break;
            }
        }

        return paramName;
    }

    /**
     * 通过参数名和参数值的映射关系来编译脚本
     * @param script 要编译的脚本
     * @param paramMap 映射关系的Map
     * @return 编译后的脚本
     */
    private String compileByParamMap(String script, Map<String, Object> paramMap) {
        String compiledScript;

        compiledScript = TemplateStringUtils.processByFreemarker(script, paramMap);

        return compiledScript;
    }
}
