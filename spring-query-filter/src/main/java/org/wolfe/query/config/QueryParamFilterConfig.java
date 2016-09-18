package org.wolfe.query.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.wolfe.query.support.QueryFilterHandlerMethodArgumentResolver;

import java.util.List;

@Configuration
public class QueryParamFilterConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private QueryFilterHandlerMethodArgumentResolver queryFilterHandlerMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(queryFilterHandlerMethodArgumentResolver);
    }
}