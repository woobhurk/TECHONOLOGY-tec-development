package com.tyfanchz.sscd01.order;

import com.tyfanchz.sscd01.common.annotation.cloud.EnableCloudApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@SpringBootApplication
@EnableCloudApp
@ComponentScan({"com.tyfanchz"})
@Slf4j
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        log.info("$$$$ OrderApplication started!");
    }
}
