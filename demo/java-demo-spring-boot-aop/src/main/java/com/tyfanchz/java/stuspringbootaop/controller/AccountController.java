package com.tyfanchz.java.stuspringbootaop.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.tyfanchz.java.stuspringbootaop.annotation.DisableRequestLog;
import com.tyfanchz.java.stuspringbootaop.entity.Account;
import com.tyfanchz.java.stuspringbootaop.utils.JsonUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description:
 * <p>Project: stu-spring-boot-aop
 *
 * @author wbh
 * @date 2020-12-07
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    @RequestMapping("/whoami")
    public String whoami() {
        return "/root";
    }

    @RequestMapping("/testKeyValue")
    @DisableRequestLog
    public Object testKeyValue(String name, String value) {
        Map<String, String> resp = new HashMap<>();
        resp.put("resp name", name);
        resp.put("resp value", value);

        return resp;
    }

    @RequestMapping("/testMap")
    public Object testMap(@RequestBody Map<String, String> req) throws Exception {
        Map<String, String> resp = new HashMap<>();
        req.forEach((key, value) -> resp.put("resp " + key, "resp " + value));

        throw new Exception("swifiwrevnier##^%&(&");
        //return resp;
    }

    @RequestMapping(value = "/testTime", method = RequestMethod.POST)
    public Object testTime(Account account) {
        System.out.println(JsonUtils.toJson(account));
        account.setId(-1);
        account.setName("noname");
        account.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        account.setCreateTime(new Date());
        return account;
    }

    public Object testTimeReq() {
        return null;
    }

    public Object testTimeResp() {
        return null;
    }
}
