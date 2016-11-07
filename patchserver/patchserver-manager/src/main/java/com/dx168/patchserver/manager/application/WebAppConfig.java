package com.dx168.patchserver.manager.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import com.dx168.patchserver.manager.common.RestResponse;
import com.dx168.patchserver.core.utils.BeanMapConvertUtil;
import com.dx168.patchserver.core.utils.BizException;
import com.dx168.patchserver.core.utils.HttpRequestUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 16/10/27.
 */
@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter implements HandlerExceptionResolver {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AppUidInterceptor appUidInterceptor;

    @Autowired
    private ServerProperties serverProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        registry.addInterceptor(appUidInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        e.printStackTrace();
        String messageStr = null;
        if (e instanceof BizException) {
            BizException bz = (BizException) e;
            messageStr = bz.getMessage();
        }

        if (messageStr == null || messageStr.trim().length() == 0) {
            messageStr = "系统异常";
        }

        RestResponse restR = new RestResponse();
        restR.setCode(-1);
        restR.setMessage(messageStr);
        if (HttpRequestUtils.isAjax(request)) {
            Map model = BeanMapConvertUtil.convertBean2Map(restR);
            if (logger.isInfoEnabled()) {
                logger.info(">>>>>resolveException ajax model: " + model);
            }
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }

        return new ModelAndView("500", "restR", restR);
    }

//    @Override
//    public void customize(ConfigurableEmbeddedServletContainer container) {
//        ErrorPage page404 = new ErrorPage(HttpStatus.NOT_FOUND,"/error/404");
//        container.addErrorPages(page404);
//    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                ErrorPage page404 = new ErrorPage(HttpStatus.NOT_FOUND,"/404");
                container.addErrorPages(page404);
            }
        };
//        return (container -> {
//            ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
//            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
//            ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
//
//            container.addErrorPages(error401Page, error404Page, error500Page);
//        });
    }
}
