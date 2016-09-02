package org.wolfe.query.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.wolfe.query.QueryParamFilter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class QueryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String FILTER_QUERY_PARAMETER = "filter";
    private static final String FILTER_REGEX = "'(.*?)([=|>|<|>=|<=])(.*?)(?:&(.*?)([=|>|<|>=|<=])(.*?))*'";
    private static final Pattern FILTER_PATTERN = Pattern.compile(FILTER_REGEX);

    private final ConversionService conversionService;

    @Autowired
    public QueryFilterHandlerMethodArgumentResolver(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(QueryParamFilter.class);
    }

    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object arg = parameter.getParameterType().newInstance();

        String filter = webRequest.getParameter(FILTER_QUERY_PARAMETER);
        if(!StringUtils.isEmpty(filter)) {
            String[] filters = FILTER_PATTERN.split(filter);
            if(filters.length % 3 == 0) {
                int i = 0;
                do {
                    String key = filters[i];
                    String op = filters[i+1];
                    String value = filters[i+2];
                    setFilterProperty(arg, key, value);
                    setFilterOperatorProperty(arg, key, op);

                    i += 3;
                } while(i < filters.length);
            }
            else {
                // TODO: Log this
            }
        }

        return arg;
    }

    private void setFilterProperty(Object target, String key, String value) throws IllegalAccessException, InvocationTargetException {
        if(!setField(target, key, value)) {
            if(!setMethod(target, "set" + key, value)) {
                // TODO: Log this.
            }
        }
    }

    private void setFilterOperatorProperty(Object target, String key, String op) throws IllegalAccessException, InvocationTargetException {
        if(!setField(target, key + "Operator", op)) {
            if(!setMethod(target, "set" + key + "Operator", op)) {
                // TODO: Log this.
            }
        }
    }

    private boolean setField(Object target, String name, String value) throws IllegalAccessException {
        Field field = ReflectionUtils.findField(target.getClass(), name);
        if(field != null) {
            Class paramType = field.getType();
            if(conversionService.canConvert(String.class, paramType)) {
                field.set(target, conversionService.convert(value, paramType));
                return true;
            }
            else {
                // TODO: Log this
            }
        }
        return false;
    }

    private boolean setMethod(Object target, String name, String value) throws IllegalAccessException, InvocationTargetException {
        Method method = ReflectionUtils.findMethod(target.getClass(), name);
        if(method != null && method.getParameterCount() == 1) {
            Class paramType = method.getParameterTypes()[0];
            if(conversionService.canConvert(String.class, paramType)) {
                method.invoke(target, conversionService.convert(value, paramType));
                return true;
            }
            else {
                // TODO: Log this
            }
        }
        else {
            // TODO: Log this
        }

        return false;
    }
}
