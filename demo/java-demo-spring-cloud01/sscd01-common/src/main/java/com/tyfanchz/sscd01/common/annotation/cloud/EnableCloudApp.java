package com.tyfanchz.sscd01.common.annotation.cloud;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@EnableDiscoveryClient
@EnableFeignClients({"com.tyfanchz"})
@EnableConfigurationProperties
// @ComponentScan 需要在 Application 中加上，在这里加上似乎会被 @SpringBootApplication 中的覆盖掉
//@ComponentScan({"com.tyfanchz"})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCloudApp {
}
