package com.yash.api_koodo.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer configureContentNegotiation() {
        return new WebMvcConfigurer() {
            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.favorPathExtension(false)
                          .favorParameter(true)
                          .parameterName("mediaType")
                          .ignoreAcceptHeader(true)
                          .useJaf(false)
                          .defaultContentType(MediaType.APPLICATION_JSON)
                          .mediaType("json", MediaType.APPLICATION_JSON);
            }
        };
    }
}
