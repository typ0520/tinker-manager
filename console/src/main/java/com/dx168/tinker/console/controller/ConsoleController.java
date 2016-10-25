package com.dx168.tinker.console.controller;

import com.dx168.tinker.console.common.Constants;
import com.dx168.tinker.console.common.RestResponse;
import com.dx168.tinker.console.service.AccountService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by tong on 15/10/24.
 */
@Controller
public class ConsoleController {
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/console",method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest req) {
        RestResponse restR = new RestResponse();
        restR.getData().put("user",req.getSession().getAttribute(Constants.SESSION_LOGIN_USER));
        return new ModelAndView("console","restR",restR);
    }
//
//    @RequestMapping(value = "/login",method = RequestMethod.POST)
//    public @ResponseBody RestResponse login(HttpServletRequest req, HttpServletResponse res, String username, String password) {
//        RestResponse restR = new RestResponse();
//        boolean result = accountService.authenticate(username,password);
//        if (result) {
//            UserInfo userInfo = accountService.getUserInfo(username);
//            restR.getData().put("user",userInfo);
//            req.getSession().setAttribute(Constants.SESSION_LOGIN_USER,userInfo);
//        }
//        else {
//            restR.setCode(1);
//            restR.setMessage("用户名或者密码不正确");
//        }
//        return restR;
//    }

}
