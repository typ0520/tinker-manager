package com.dx168.tmserver.manager.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tong on 15/10/21.
 */
@SpringBootApplication
@ComponentScan(value = {"com.dx168.tmserver"})
public class ManagerApplication extends SpringBootServletInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ManagerApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ManagerApplication.class).properties(getSpringExtentionConfig(null));
    }

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    public static Properties getSpringExtentionConfig(String defaultPath) {
        InputStream inputStream = ManagerApplication.class.getClassLoader().getResourceAsStream("location.properties");
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


    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Bean
    public AppUidInterceptor appUidInterceptor() {
        return new AppUidInterceptor();
    }
}
