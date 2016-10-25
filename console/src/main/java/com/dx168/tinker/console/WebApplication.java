package com.dx168.tinker.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tong on 15/10/21.
 */
@SpringBootApplication
@ComponentScan(value = {"com.dx168.tinker.console"})
@ImportResource(value = {"classpath:applicationContext.xml"})
public class WebApplication extends SpringBootServletInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(WebApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplication.class).properties(getSpringExtentionConfig(null));
    }

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    public static Properties getSpringExtentionConfig(String defaultPath) {
        InputStream inputStream = WebApplication.class.getClassLoader().getResourceAsStream("location.properties");
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (props.getProperty("spring.config.location") == null && defaultPath != null) {
            props.put("spring.config.location", defaultPath);
        }
        LOG.info("LQ9836110: use spring.config.location=" + props.getProperty("spring.config.location"));
        return props;
    }
}
