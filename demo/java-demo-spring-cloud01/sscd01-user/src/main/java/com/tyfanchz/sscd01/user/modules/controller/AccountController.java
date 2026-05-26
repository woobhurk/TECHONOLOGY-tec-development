package com.tyfanchz.sscd01.user.modules.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-23
 */
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @GetMapping("/hello")
    public String hello(String name) {
        return "OK! " + name;
    }
}
