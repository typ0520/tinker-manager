package com.dx168.tmserver.manager.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import com.dx168.tmserver.manager.common.Constants;
import com.dx168.tmserver.core.utils.HttpRequestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by tong on 16/10/25.
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ServerProperties serverProperties;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "");
        boolean isNeedFilter = HttpRequestUtils.isInclude(uri,"/console/**","/app/**","/patch/**");

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
        if (req.getQueryString() != null) {
            redirect = redirect + "?" + req.getQueryString();
            redirect = HttpRequestUtils.urlEncode(redirect);
        }
        return serverProperties.getServletPrefix() + "/login?redirect=" + redirect;
    }
}
