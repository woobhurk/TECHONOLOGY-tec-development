package com.tyfanchz.sscd01.order.modules.controller;

import com.tyfanchz.sscd01.order.modules.remote.AccountClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private AccountClient accountClient;

    @GetMapping("/order")
    public String getOrder() {
        String result = accountClient.sayHello("wbh");
        log.info("$$$$ /order/order: {}", result);
        return "account: " + result;
    }
}
