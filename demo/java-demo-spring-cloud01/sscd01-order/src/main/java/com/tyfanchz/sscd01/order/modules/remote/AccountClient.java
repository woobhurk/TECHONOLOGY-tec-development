package com.tyfanchz.sscd01.order.modules.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>Description:
 * <p>Project: stu-spring-cloud-demo01
 *
 * @author wbh
 * @date 2021-06-24
 */
@FeignClient(name = "${feign.server.name}", url = "${feign.server.url}",
        path = "/sscd01-user/account")
public interface AccountClient {
    @GetMapping("/hello")
    String sayHello(@RequestParam("name") String name);
}
