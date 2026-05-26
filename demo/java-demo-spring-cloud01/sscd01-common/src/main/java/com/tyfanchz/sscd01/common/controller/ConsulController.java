package com.tyfanchz.sscd01.common.controller;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@RestController
@RequestMapping("/consul")
@Slf4j
public class ConsulController {
    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        log.info("$$$$ ConsulController {}", env.getProperty("server.servlet.context-path"));
    }

    @GetMapping("/checkHealth")
    public String checkHealth() {
        return "OK";
    }
}
