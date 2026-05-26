package com.tyfanchz.dynamicscript.enginev1.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptParam {
    /**
     * 参数名称
     * @return 参数名称
     */
    String value();

    /**
     * 参数名称
     * @return 参数名称
     */
    String name()  default "";
}
