package com.tyfanchz.sscd01.common.config;

import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@Configuration
public class FeignConfiguration {
    /**
     * Spring Cloud Gateway 默认使用了 WebFlux 组件，因此 {@link org.springframework.boot.autoconfigure.http.HttpMessageConverters} 不会自动注入，导致 Feign 调用失败，故需要手动创建这个东西。
     *
     * @param converters 转换器
     * @return 转换器
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters converters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}
