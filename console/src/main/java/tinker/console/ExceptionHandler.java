package tinker.console;

import tinker.console.common.BizException;
import tinker.console.common.RestResponse;
import tinker.console.utils.BeanMapConvertUtil;
import tinker.console.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by tong on 8/25/15.
 */
@Component
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@ConditionalOnWebApplication
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@Configuration
public class ExceptionHandler implements HandlerExceptionResolver,EmbeddedServletContainerCustomizer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServerProperties properties;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        e.printStackTrace();
        String messageStr = "系统异常";
        if (e instanceof BizException) {
            BizException bz = (BizException) e;
            messageStr = bz.getMessage();
        }

        final String message = messageStr;
        if (HttpRequestUtils.isAjax(request)) {
            RestResponse restR = new RestResponse();
            restR.setCode(-1);
            restR.setMessage(message);

            Map model = BeanMapConvertUtil.convertBean2Map(restR);
            if (logger.isInfoEnabled()) {
                logger.info(">>>>>resolveException ajax model: " + model);
            }
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }

        return new ModelAndView("redirect:" + getRedirectUrl(message));
    }

    private String getRedirectUrl(String message) {
        return "/error?message=" + HttpRequestUtils.urlEncode(message);
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        ErrorPage page404 = new ErrorPage(HttpStatus.NOT_FOUND,this.properties.getServletPrefix() + "/pageNotFound");
        container.addErrorPages(page404);
    }
}
