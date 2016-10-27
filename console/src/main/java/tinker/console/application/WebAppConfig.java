package tinker.console.application;

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
import tinker.console.common.BizException;
import tinker.console.common.RestResponse;
import tinker.console.utils.BeanMapConvertUtil;
import tinker.console.utils.HttpRequestUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 16/10/27.
 */
@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter implements HandlerExceptionResolver,EmbeddedServletContainerCustomizer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AppUidInterceptor appUidInterceptor;

    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Bean
    public AppUidInterceptor appUidInterceptor() {
        return new AppUidInterceptor();
    }

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
        String messageStr = "系统异常";
        if (e instanceof BizException) {
            BizException bz = (BizException) e;
            messageStr = bz.getMessage();
        }

        final String message = messageStr;
        boolean apiRequest = false;
        if (HttpRequestUtils.isAjax(request) || (apiRequest = HttpRequestUtils.isApiRequest(request))) {
            RestResponse restR = new RestResponse();
            restR.setCode(-1);
            restR.setMessage(message);
            Map model = BeanMapConvertUtil.convertBean2Map(restR);

            if (apiRequest) {
                model.put("data",null);
            }
            if (logger.isInfoEnabled()) {
                logger.info(">>>>>resolveException ajax model: " + model);
            }
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }

        return new ModelAndView("redirect:/500?message=" + HttpRequestUtils.urlEncode(message));
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        ErrorPage page404 = new ErrorPage(HttpStatus.NOT_FOUND,this.serverProperties.getServletPrefix() + "/404");
        container.addErrorPages(page404);
        ErrorPage page500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,this.serverProperties.getServletPrefix() + "/500");
        container.addErrorPages(page500);
    }
}
