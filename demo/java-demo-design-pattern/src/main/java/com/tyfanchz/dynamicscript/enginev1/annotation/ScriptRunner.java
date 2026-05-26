package com.tyfanchz.dynamicscript.enginev1.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 脚本配置注解
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptRunner {
    /**
     * 使用的引擎
     * @return 使用的引擎
     */
    String value() default "";

    /**
     * 使用的引擎
     * @return 使用的引擎
     */
    String engine() default "";
}
