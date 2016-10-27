package tinker.console.controller;

import tinker.console.common.RestResponse;
import tinker.console.utils.BeanMapConvertUtil;
import tinker.console.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by shenyujie on 2015/8/28.
 */
@Controller
public class ErrorController {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 错误页面
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/500", method = RequestMethod.GET)
    public ModelAndView error(HttpServletRequest req,String message,ModelMap map) {
        RestResponse restR = new RestResponse();
        if (map != null) {
            logger.info("<<< error map: " + map);
        }

        if (message != null) {
            message = HttpRequestUtils.urlDecode(message);
        }
        else {
            message = "系统异常";
        }

        restR.setMessage(message);

        if (HttpRequestUtils.isAjax(req)) {
            Map model = BeanMapConvertUtil.convertBean2Map(restR);
            if (logger.isInfoEnabled()) {
                logger.info(">>>>>resolveException ajax model: " + model);
            }
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }

        if (logger.isInfoEnabled()) {
            logger.info("filter: message: " + message);
        }

        return new ModelAndView("error", "restR", restR);
    }

    @RequestMapping("/404")
    public String pageNotFound() {
        return "404";
    }
}
