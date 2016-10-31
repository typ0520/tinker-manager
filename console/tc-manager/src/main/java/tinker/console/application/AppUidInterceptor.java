package tinker.console.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import tinker.console.common.Constants;
import tinker.console.core.domain.AppInfo;
import tinker.console.core.domain.BasicUser;
import tinker.console.core.utils.BizException;
import tinker.console.service.AccountService;
import tinker.console.service.AppService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by tong on 16/10/25.
 */
@Component
public class AppUidInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AppService appService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String appUid = req.getParameter("appUid");
        if (appUid != null && appUid.trim().length() > 0) {
            AppInfo appInfo = appService.findByUid(appUid);
            if (appInfo == null) {
                throw new BizException("应用不存在");
            }

            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Integer userId = accountService.getRootUserId(basicUser);
            if (appInfo != null && appInfo.getUserId() != userId) {
                //检测appUid对应的应用是否属于当前登录用户的应用
                throw new BizException("应用不存在");
            }
        }
        return super.preHandle(req, res, handler);
    }
}
