package org.wolfe.query.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.wolfe.query.QueryParamOperator;
import org.wolfe.query.pattern.DefaultQueryFilterPatternProvider;
import org.wolfe.query.pattern.QueryFilterPatternProvider;
import org.wolfe.query.support.QueryFilterHandlerMethodArgumentResolver;
import org.wolfe.query.validator.QueryParamOperatorValidator;

import javax.validation.ConstraintValidator;
import java.util.List;

@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class QueryParamFilterConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private QueryFilterHandlerMethodArgumentResolver queryFilterHandlerMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(queryFilterHandlerMethodArgumentResolver);
    }

    @Bean
    public QueryFilterPatternProvider queryFilterPatternProvider() {
        return new DefaultQueryFilterPatternProvider();
    }

    @Bean
    public ConstraintValidator<QueryParamOperator, String> queryParamOperatorValidator() {
        return new QueryParamOperatorValidator();
    }

    @Bean
    public QueryFilterHandlerMethodArgumentResolver queryFilterHandlerMethodArgumentResolver(QueryFilterPatternProvider queryFilterPatternProvider,
                                                                                                ConstraintValidator<QueryParamOperator, String> queryParamOperatorValidator,
                                                                                                ConversionService conversionService,
                                                                                                Validator validator) {
        return new QueryFilterHandlerMethodArgumentResolver(queryFilterPatternProvider, queryParamOperatorValidator,
                conversionService, validator);
    }
}
