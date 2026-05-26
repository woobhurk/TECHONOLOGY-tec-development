package com.tyfanchz.sscd01.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@Component
public class PropertiesUtils {
    private static Environment env;

    @Autowired
    public PropertiesUtils(Environment env) {
        PropertiesUtils.env = env;
    }

    public static String getProp(String key) {
        return env.getProperty(key);
    }

    public static Environment getEnv() {
        return env;
    }
}
