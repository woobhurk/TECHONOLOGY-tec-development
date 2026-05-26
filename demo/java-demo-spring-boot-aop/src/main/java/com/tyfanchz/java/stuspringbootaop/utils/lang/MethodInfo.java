package com.tyfanchz.java.stuspringbootaop.utils.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * <p>Description:
 * <p>Project: stu-spring-boot-aop
 *
 * @author wbh
 * @date 2020-12-09
 */
public class MethodInfo {
    private List<Annotation> annotationList;
    private String name;
    private List<Parameter> parameterList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }
}
