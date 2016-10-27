package tinker.console.web;

import tinker.console.common.BizAssert;
import tinker.console.common.BizException;
import tinker.console.common.Constants;
import tinker.console.common.RestResponse;
import tinker.console.domain.BasicUser;
import tinker.console.service.AccountService;
import tinker.console.utils.HttpRequestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by tong on 15/10/24.
 */
@Controller
public class AccountController {
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public ModelAndView login(String redirect,String msg) {
        RestResponse restR = new RestResponse();
        restR.getData().put("redirect", HttpRequestUtils.urlDecode(redirect));
        restR.setMessage(HttpRequestUtils.urlDecode(msg));
        return new ModelAndView("login","restR",restR);
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest req, String redirect, String username, String password) {
        RestResponse restR = new RestResponse();
        boolean result = accountService.authenticate(username,password);
        if (result) {
            BasicUser basicUser = accountService.findByUsername(username);
            restR.getData().put("basicUser", basicUser);
            req.getSession().setAttribute(Constants.SESSION_LOGIN_USER, basicUser);

            if (StringUtils.isEmpty(redirect)) {
                return new ModelAndView("redirect:/console");
            }
            else {
                return new ModelAndView("redirect:" + redirect);
            }
        }
        else {
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
        RestResponse restR = new RestResponse();
        restR.getData().put("redirect", HttpRequestUtils.urlDecode(redirect));
        restR.setMessage(HttpRequestUtils.urlDecode(msg));
        return new ModelAndView("regist","restR",restR);
    }

    @RequestMapping(value = "/regist",method = RequestMethod.POST)
    public ModelAndView regist(HttpServletRequest req,String redirect,String username, String password) {
        try {
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
            req.getSession().setAttribute(Constants.SESSION_LOGIN_USER,basicUser);
            return new ModelAndView("redirect:/");
        } catch (BizException e) {
            return new ModelAndView("redirect:/regist?redirect=" + redirect + "&msg=" + HttpRequestUtils.urlEncode(e.getMessage()));
        }
    }
}
