package com.dx168.tinker.console.controller;

import com.dx168.tinker.console.common.Constants;
import com.dx168.tinker.console.common.RestResponse;
import com.dx168.tinker.console.bean.UserInfo;
import com.dx168.tinker.console.service.AccountService;
import com.dx168.tinker.console.utils.HttpRequestUtils;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by tong on 15/10/24.
 */
@Controller
public class AccountController {
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public ModelAndView login(String redirect,String error) {
        RestResponse restR = new RestResponse();
        restR.getData().put("redirect", HttpRequestUtils.urlDecode(redirect));
        restR.getData().put("error",HttpRequestUtils.urlDecode(error));
        return new ModelAndView("login","restR",restR);
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest req,String redirect,String username, String password) {
        RestResponse restR = new RestResponse();
        boolean result = accountService.authenticate(username,password);
        if (result) {
            UserInfo userInfo = accountService.getUserInfo(username);
            restR.getData().put("user",userInfo);
            req.getSession().setAttribute(Constants.SESSION_LOGIN_USER,userInfo);

            if (StringUtils.isEmpty(redirect)) {
                return new ModelAndView("redirect:/console");
            }
            else {
                return new ModelAndView("redirect:" + redirect);
            }
        }
        else {
//            restR.getData().put("error","用户名或者密码不正确");
//            restR.getData().put("redirect",redirect);
//            return new ModelAndView("login","restR",restR);
            return new ModelAndView("redirect:/login?redirect=" + redirect + "&error=" + HttpRequestUtils.urlEncode("用户名或者密码不正确"));
        }
    }

//    @RequestMapping(value = "/hello",method = RequestMethod.GET)
//    public  @ResponseBody
//    RestResponse hello(String name) {
//        RestResponse restR = new RestResponse();
//        Map<String,Object> data = new HashMap<>();
//        restR.setData(data);
//        data.put("name",name);
//        return restR;
//    }
//
//    @RequestMapping(value = "/welcome",method = RequestMethod.GET)
//    public ModelAndView welcome(String name) {
//        RestResponse restR = new RestResponse();
//        Map<String,Object> data = new HashMap<>();
//        restR.setData(data);
//        data.put("name",name);
//        return new ModelAndView("welcome","restR",restR);
//    }
//
//    @RequestMapping("/")
//    public String welcomes(Map<String, Object> model) {
//        model.put("time", new Date());
//        model.put("message", "哈哈哈");
//        return "welcome";
//    }
}
