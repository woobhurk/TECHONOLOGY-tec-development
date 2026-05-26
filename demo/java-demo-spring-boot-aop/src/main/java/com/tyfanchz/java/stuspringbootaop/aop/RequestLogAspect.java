package com.tyfanchz.java.stuspringbootaop.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import com.tyfanchz.java.stuspringbootaop.utils.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * <p>Description:
 * <p>Project: stu-spring-boot-aop
 *
 * @author wbh
 * @date 2020-12-07
 */
@Component
@Aspect
public class RequestLogAspect {
    private long methodStartTime = 0;

    /**
     * 切入带有 {@link org.springframework.web.bind.annotation.RequestMapping} 注解
     * 且不带有 {@link com.tyfanchz.java.stuspringbootaop.annotation.DisableRequestLog} 注解的方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "&& !@annotation(com.tyfanchz.java.stuspringbootaop.annotation.DisableRequestLog)")
    public void annotatedByRequestMapping() {}

    /**
     * 切入带有 {@link com.tyfanchz.java.stuspringbootaop.annotation.EnableRequestLog} 注解的方法
     */
    @Pointcut("@annotation(com.tyfanchz.java.stuspringbootaop.annotation.EnableRequestLog)")
    public void annotetedByCustom() {}

    @Around("annotatedByRequestMapping()")
    public Object aroundRequest(ProceedingJoinPoint joinPoint) {
        Object result;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        try {
            this.showMethodInfo(clazz, method);
            this.showMethodParam(signature.getParameterNames(), joinPoint.getArgs());
            this.recordMethodStartTime();
            result = joinPoint.proceed();
            this.showMethodUsedTime();
            this.showMethodResult(method, result);
        } catch (Throwable t) {
            showMethodError(clazz, method, t);
            result = null;
        }

        return result;
    }

    private void showMethodInfo(Class<?> clazz, Method method) {
        System.out.printf("#### [RequestLog] Method name: %s%n",
                this.buildMethodInfo(clazz, method));
    }

    private void showMethodParam(String[] paramNames, Object[] paramValues) {
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            paramMap.put(paramNames[i], paramValues[i]);
        }
        System.out.printf("#### [RequestLog] Method param: %s%n", JsonUtils.toJson(paramMap));
    }

    private void recordMethodStartTime() {
        this.methodStartTime = System.currentTimeMillis();
    }

    private void showMethodUsedTime() {
        if (this.methodStartTime > 0) {
            long usedTime = System.currentTimeMillis() - this.methodStartTime;
            System.out.printf("#### [RequestLog] Method used time: %d ms.%n", usedTime);
            this.methodStartTime = 0;
        }
    }

    private void showMethodResult(Method method, Object result) {
        if (!method.getName().matches("(?i).*(list|page).*")) {
            System.out.printf("#### [RequestLog] Method returned: %s%n", JsonUtils.toJson(result));
        }
    }

    private void showMethodError(Class<?> clazz, Method method, Throwable t) {
        System.err.printf("#### [RequestLog] Method ERROR: %s%n",
                this.buildMethodInfo(clazz, method));
        t.printStackTrace();
    }

    private String buildMethodInfo(Class<?> clazz, Method method) {
        StringBuffer infoSb = new StringBuffer();
        String className = clazz.getName();
        String methodName = method.getName();
        infoSb.append(className).append("#").append(methodName);

        Parameter[] parameters = method.getParameters();
        StringBuffer paramInfoSb = new StringBuffer();
        for (Parameter parameter : parameters) {
            paramInfoSb.append(parameter.getType().getSimpleName());
            if (parameter.isVarArgs()) {
                paramInfoSb.append("..");
            }
            paramInfoSb.append(" ").append(parameter.getName()).append(", ");
        }
        infoSb.append("(")
                .append(paramInfoSb.toString().replaceAll(", *$", ""))
                .append(")");

        return infoSb.toString();
    }
}
