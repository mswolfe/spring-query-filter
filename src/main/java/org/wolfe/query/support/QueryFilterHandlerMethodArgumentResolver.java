package org.wolfe.query.support;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.support.ReflectionHelper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String FILTER_QUERY_PARAMETER = "filter";
    private static final String FILTER_REGEX = "'(.+?)(>=|<=|=|<|>)(.+?)(?:&(.+?)(>=|<=|=|<|>)(.+?))*'";
    private static final Pattern FILTER_PATTERN = Pattern.compile(FILTER_REGEX);

    private final ConversionService conversionService;

    private static final Log log = LogFactory.getLog(QueryFilterHandlerMethodArgumentResolver.class);

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
            Matcher matcher = FILTER_PATTERN.matcher(filter);
            if(matcher.matches() && matcher.groupCount() % 3 == 0) {
                int i = 1;
                do {
                    String key = matcher.group(i);
                    String op = matcher.group(i+1);
                    String value = matcher.group(i+2);

                    // Silly java regex implementation returns null for optional
                    // groups instead of not matching them at all...
                    if(key != null && op != null && value != null) {
                        setFilterProperty(arg, key, op, value);
                        setFilterOperatorProperty(arg, key, op, value);
                    }

                    i += 3;
                } while(i < matcher.groupCount());
            }
            else {
                if(log.isWarnEnabled()) {
                    log.warn("filter query parameter could not be parsed successfully.");
                }
            }
        }

        return arg;
    }

    private boolean setFilterProperty(Object target, String key, String op, String value) {
        if(setBeanProperty(target, key, value)) {
            return true;
        }
        else {
            if(log.isErrorEnabled()) {
                log.error(String.format("Failed to set the filter property for '%s%s%s'", key, op, value));
            }
        }

        return false;
    }

    private boolean setFilterOperatorProperty(Object target, String key, String op, String value) {
        if(setBeanProperty(target, key + "Operator", op)) {
            return true;
        }
        else {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Failed to set the filter operator for '%s%s%s'", key, op, value));
            }
        }

        return false;
    }

    /**
     * Sets the given target bean's name property to the given value.
     *
     * @param target
     * @param name
     * @param value
     * @return
     */
    private boolean setBeanProperty(Object target, String name, String value) {
        PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(target);
        if(accessor != null && accessor.isWritableProperty(name)) {
            Class paramType = accessor.getPropertyType(name);
            if(paramType != null) {
                if (conversionService.canConvert(String.class, paramType)) {
                    try {
                        accessor.setPropertyValue(name, conversionService.convert(value, paramType));
                        return true;
                    } catch (BeansException be) {
                        if (log.isErrorEnabled()) {
                            log.warn(String.format("Failed to invoke setter method for '%s'", name));
                        }
                    }
                } else {
                    if(log.isWarnEnabled()) {
                        log.warn(String.format("ConversionService cannot convert filter parameter '%s' to type '%s'", name, paramType.getName()));
                    }
                }
            }
            else {
                if(log.isErrorEnabled()) {
                    log.error(String.format("Failed to invoke setter method for '%s'; could not determine parameter type.", name));
                }
            }
        }
        else {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Property '%s' is not writable", name));
            }
        }

        return false;
    }
}
