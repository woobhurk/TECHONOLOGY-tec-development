package com.tyfanchz.sscd01.user;

import com.tyfanchz.sscd01.common.annotation.cloud.EnableCloudApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCloudApp
@ComponentScan({"com.tyfanchz"})
@Slf4j
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        log.info("$$$$ UserApplication started!");
    }
}
