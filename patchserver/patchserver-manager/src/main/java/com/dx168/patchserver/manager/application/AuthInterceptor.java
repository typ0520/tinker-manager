package com.dx168.patchserver.manager.application;

import com.dx168.patchserver.manager.common.Constants;
import com.dx168.patchserver.core.utils.HttpRequestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by tong on 16/10/25.
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "");
        boolean isNeedFilter = HttpRequestUtils.isInclude(uri,"/app/**","/patch/**","/version/**","/tester/**","/modelblacklist/**","/channel/**","/child_user/**","/full_update/**");

        if (!isNeedFilter) {
            return true;
        }
        if (req.getSession().getAttribute(Constants.SESSION_LOGIN_USER) == null) {
            String redirectUrl = getRedirectUrl(req);
            if (HttpRequestUtils.isAjax(req)) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                res.addHeader("Ajax-Response-Redirect", redirectUrl);
            }
            else {
                res.sendRedirect(req.getContextPath() + redirectUrl);
            }
            return false;
        }
        return super.preHandle(req, res, handler);
    }

    private String getRedirectUrl(HttpServletRequest req) {
        String redirect = req.getRequestURI();
        if (redirect.startsWith(req.getContextPath())
                && !"/".equals(req.getContextPath())
                && StringUtils.isNotEmpty(req.getContextPath())) {
            redirect = redirect.substring(req.getContextPath().length(),redirect.length());
        }
        if (req.getQueryString() != null) {
            redirect = redirect + "?" + req.getQueryString();
            redirect = HttpRequestUtils.urlEncode(redirect);
        }
        return "/login?redirect=" + redirect;
    }
}
