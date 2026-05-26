package com.tyfanchz.dynamicscript.utils;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.text.StringSubstitutor;

/**
 * 模板字符串处理类
 */
public class TemplateStringUtils {
    private TemplateStringUtils() {}

    private static class FreemarkerConfig {
        private static Configuration configuration = new Configuration(Configuration.VERSION_2_3_29);

        static {
            // UTF-8
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            // 忽略异常，继续处理
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
            //configuration.setTemplateExceptionHandler(new CustomTemplateExceptionHandler());
        }
    }

    private static class CustomTemplateExceptionHandler implements TemplateExceptionHandler {
        @Override
        public void handleTemplateException(TemplateException te, Environment env, Writer out) {
            System.out.println("@@@@ Ignore error: " + te.getMessageWithoutStackTop());
        }
    }

    /**
     * 使用Freemarker处理
     * @param src 源字符串
     * @param paramMap 参数
     * @return 处理后的字符串
     */
    public static String processByFreemarker(String src, Map<String, Object> paramMap) {
        Template template;
        StringWriter resultWriter = new StringWriter();
        String result;

        try {
            template = new Template("srcTmpl", src,
                FreemarkerConfig.configuration);
            template.process(paramMap, resultWriter);
            result = resultWriter.toString();
        } catch (Exception e) {
            result = src;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 使用commons-text的工具类来处理
     * @param src 源字符串
     * @param paramMap 参数
     * @return 处理后的字符串
     */
    public static String processBySubstitution(String src, Map<String, Object> paramMap) {
        StringSubstitutor substitutor = new StringSubstitutor(paramMap);
        String result;

        result = substitutor.replace(src);

        return result;
    }
}
