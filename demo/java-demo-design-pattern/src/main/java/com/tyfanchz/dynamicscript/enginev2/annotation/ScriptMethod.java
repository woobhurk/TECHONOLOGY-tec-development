package com.tyfanchz.dynamicscript.enginev2.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法配置注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptMethod {
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

    /**
     * 脚本
     * @return 脚本
     */
    String script() default "";

    /**
     * 多行脚本
     * @return 多行脚本
     */
    String[] scripts() default {};
}
