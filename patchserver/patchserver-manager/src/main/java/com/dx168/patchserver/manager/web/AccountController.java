package com.dx168.patchserver.manager.web;

import com.dx168.patchserver.core.domain.AppInfo;
import com.dx168.patchserver.core.domain.ChildUserApp;
import com.dx168.patchserver.manager.service.AccountService;
import com.dx168.patchserver.manager.common.Constants;
import com.dx168.patchserver.manager.common.RestResponse;
import com.dx168.patchserver.core.domain.BasicUser;
import com.dx168.patchserver.core.utils.BizAssert;
import com.dx168.patchserver.core.utils.BizException;
import com.dx168.patchserver.core.utils.HttpRequestUtils;
import com.dx168.patchserver.manager.service.AppService;
import com.dx168.patchserver.manager.utils.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by tong on 15/10/24.
 */
@Controller
public class AccountController {
    @Value("${open_regist}")
    private boolean openRegist;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AppService appService;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest req,String redirect,String msg) {
        RestResponse restR = new RestResponse();
        restR.getData().put("redirect", HttpRequestUtils.urlDecode(redirect));
        restR.getData().put("openRegist",openRegist);
        restR.setMessage(HttpRequestUtils.urlDecode(msg));

        Cookie cokLoginName = CookieUtil.getCookieByName(req, Constants.COOKIE_LOGINNAME);
        Cookie cokLoginPwd = CookieUtil.getCookieByName(req, Constants.COOKIE_LOGINPWD);
        if (cokLoginName != null
                && cokLoginPwd != null
                && StringUtils.isNotEmpty(cokLoginName.getValue())
                && StringUtils.isNotEmpty(cokLoginPwd.getValue())) {
            restR.getData().put(Constants.COOKIE_LOGINNAME,cokLoginName.getValue());
            restR.getData().put(Constants.COOKIE_LOGINPWD,cokLoginPwd.getValue());
        }
        return new ModelAndView("login","restR",restR);
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest req, HttpServletResponse res, String redirect, String username, String password, String rememberPwd) {
        RestResponse restR = new RestResponse();
        boolean result = accountService.authenticate(username,password);
        if (result) {
            BasicUser basicUser = accountService.findByUsername(username);
            restR.getData().put("basicUser", basicUser);
            req.getSession().setAttribute(Constants.SESSION_LOGIN_USER, basicUser);

            if ("on".equals(rememberPwd)) {
                //记录一个月
                CookieUtil.addCookie(res, Constants.COOKIE_LOGINNAME, username, Constants.COOKIE_EXPIRY_DATE);
                CookieUtil.addCookie(res, Constants.COOKIE_LOGINPWD, password, Constants.COOKIE_EXPIRY_DATE);
            }
            if (StringUtils.isEmpty(redirect)) {
                return new ModelAndView("redirect:/app/list");
            }
            else {
                return new ModelAndView("redirect:" + redirect);
            }
        }
        else {
            CookieUtil.addCookie(res, Constants.COOKIE_LOGINNAME, null, 0); // 清除Cookie
            CookieUtil.addCookie(res, Constants.COOKIE_LOGINPWD, null, 0); // 清除Cookie
            return new ModelAndView("redirect:/login?redirect=" + redirect + "&msg=" + HttpRequestUtils.urlEncode("用户名或者密码不正确"));
        }
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest req) {
        req.getSession().removeAttribute(Constants.SESSION_LOGIN_USER);
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/regist",method = RequestMethod.GET)
    public ModelAndView regist(String redirect,String msg) {
        if (!openRegist) {
            throw new BizException("暂不接受注册");
        }

        RestResponse restR = new RestResponse();
        restR.getData().put("redirect", HttpRequestUtils.urlDecode(redirect));
        restR.setMessage(HttpRequestUtils.urlDecode(msg));
        return new ModelAndView("regist","restR",restR);
    }

    @RequestMapping(value = "/regist",method = RequestMethod.POST)
    public ModelAndView regist(HttpServletRequest req,HttpServletResponse res,String redirect,String username, String password) {
        try {
            if (!openRegist) {
                throw new BizException("暂不接受注册");
            }
            BizAssert.isVaildUsername(username,"用户名格式不正确(以字母开头，长度在5~18之间，只能包含字符、数字和下划线)");
            BizAssert.isVaildUsername(password,"密码格式不正确(以字母开头，长度在5~18之间，只能包含字符、数字和下划线)");
            BasicUser basicUser = accountService.findByUsername(username);
            if (basicUser != null) {
                throw new BizException(username + " 已被占用");
            }
            basicUser = new BasicUser();
            basicUser.setUsername(username);
            basicUser.setPassword(password);
            accountService.save(basicUser);

            req.getSession().removeAttribute(Constants.SESSION_LOGIN_USER);
            CookieUtil.addCookie(res, Constants.COOKIE_LOGINNAME, null, 0); // 清除Cookie
            CookieUtil.addCookie(res, Constants.COOKIE_LOGINPWD, null, 0); // 清除Cookie
            //req.getSession().setAttribute(Constants.SESSION_LOGIN_USER,basicUser);
            return new ModelAndView("redirect:/");
        } catch (BizException e) {
            return new ModelAndView("redirect:/regist?redirect=" + redirect + "&msg=" + HttpRequestUtils.urlEncode(e.getMessage()));
        }
    }

    @RequestMapping(value = "/child_user/add",method = RequestMethod.POST)
    public @ResponseBody RestResponse regist(HttpServletRequest req, HttpServletResponse res, String username, String password) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.isVaildUsername(username,"用户名格式不正确(以字母开头，长度在5~18之间，只能包含字符、数字和下划线)");
            BizAssert.isVaildUsername(password,"密码格式不正确(以字母开头，长度在5~18之间，只能包含字符、数字和下划线)");
            BasicUser basicUser = accountService.findByUsername(username);
            if (basicUser != null) {
                throw new BizException(username + " 已被占用");
            }
            BasicUser rootUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            basicUser = new BasicUser();
            basicUser.setParentId(rootUser.getId());
            basicUser.setUsername(username);
            basicUser.setPassword(password);
            accountService.save(basicUser);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/child_user/remove",method = RequestMethod.POST)
    public @ResponseBody RestResponse regist(HttpServletRequest req, HttpServletResponse res, Integer id) {
        RestResponse restR = new RestResponse();
        try {
            BasicUser rootUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            accountService.removeChildAcc(rootUser,id);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/child_user/apps",method = RequestMethod.GET)
    public @ResponseBody RestResponse getChildUserApps(Integer userId) {
        RestResponse restR = new RestResponse();
        try {
            BasicUser basicUser = accountService.findById(userId);
            if (!basicUser.isChildAccount()) {
                throw new BizException("只有子账号才有对应信息");
            }
            List<ChildUserApp> childUserAppList = appService.findAllChildUserAppByUserId(userId);
            restR.getData().put("childUserAppList",childUserAppList);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/child_user/apps",method = RequestMethod.POST)
    public @ResponseBody RestResponse saveChildUserApps(HttpServletRequest req,String appUids,Integer userId) {
        RestResponse restR = new RestResponse();
        try {
            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            if (basicUser.isChildAccount()) {
                throw new BizException("子账号没有权限");
            }
            String[] appUidArray = appUids.split(",");
            ArrayList<String> appUidList = new ArrayList<>();
            if (appUidArray != null) {
                for (int i = 0; i < appUidArray.length; i++) {
                    String appUid = appUidArray[i];

                    if (!StringUtils.isBlank(appUid)) {
                        appUidList.add(appUid);
                    }
                }
            }
            appService.createChildUserAppMapping(basicUser,userId,appUidList);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/child_user/list",method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest req) {
        RestResponse restR = new RestResponse();

        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        if (basicUser.isChildAccount()) {
            throw new BizException("子账号没有权限");
        }
        List<AppInfo> appInfoList = appService.findAllAppInfoByUser(basicUser);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfoList",appInfoList);
        List<BasicUser> childAccList = accountService.findAllChildUser(basicUser.getId());
        restR.getData().put("childAccList",childAccList);

        Map<Integer,List<String>> userIdAppUidMap = new HashMap<>();
        for (BasicUser childUser : childAccList) {
            List<String> appUidList = appService.findAllChildUserAppUidsByUserId(childUser.getId());
            userIdAppUidMap.put(childUser.getId(),appUidList);
        }
        restR.getData().put("userIdAppUidMap",userIdAppUidMap);

        return new ModelAndView("child_list","restR",restR);
    }
}
