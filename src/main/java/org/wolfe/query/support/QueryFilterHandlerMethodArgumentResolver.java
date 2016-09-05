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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.wolfe.query.QueryParamFilter;
import org.wolfe.query.pattern.QueryFilterPatternProvider;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final String filterParameterName;
    private final Pattern filterPattern;
    private final String filterParameterDelimiter;

    private final ConversionService conversionService;
    private final Validator validator;

    private static final Log log = LogFactory.getLog(QueryFilterHandlerMethodArgumentResolver.class);

    @Autowired
    public QueryFilterHandlerMethodArgumentResolver(QueryFilterPatternProvider queryFilterPatternProvider, ConversionService conversionService, Validator validator) {
        this.filterPattern = queryFilterPatternProvider.getPattern();
        this.filterParameterDelimiter = queryFilterPatternProvider.getParameterDelimiter();
        this.filterParameterName = queryFilterPatternProvider.getParameterName();

        this.conversionService = conversionService;
        this.validator = validator;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(QueryParamFilter.class);
    }

    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object arg = parameter.getParameterType().newInstance();
        String filter = webRequest.getParameter(filterParameterName);

        // Populate the created object with the values from the filter argument.
        populateFilterArgument(arg, filter);

        // Perform validation on the created object.
        validateFilterArgument(arg, parameter);

        return arg;
    }

    /**
     * 1. Use the filter query parameter and strip the apostrophe from the start and end
     * 2. Split the string based upon the parameter delimiter
     * 3. Use the pattern regex to match each parameter, "<key><op><value>"
     * 4. Try to populate the key's property in the target object.
     * @param arg
     * @param filter
     */
    private void populateFilterArgument(Object arg, String filter) {
        if(!StringUtils.isEmpty(filter) && filter.length() > 2 && filter.startsWith("'") && filter.endsWith("'")) {
            String withOutParens = filter.substring(1, filter.length() - 1);
            String[] params = withOutParens.split(filterParameterDelimiter);
            if(log.isInfoEnabled()) {
                log.info(String.format("Found %s filter query parameters", params.length));
            }

            for(String param : params) {
                Matcher matcher = filterPattern.matcher(param);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    String key = matcher.group(1);
                    String op = matcher.group(2);
                    String value = matcher.group(3);

                    setFilterProperty(arg, key, op, value);
                    setFilterOperatorProperty(arg, key, op, value);
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn(String.format("Failed to parse filter query parameter '%s'", param));
                    }
                }
            }
        }
        else {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Failed to trim apostrophes from the filter '%s'", filter));
            }
        }
    }

    private void validateFilterArgument(Object arg, MethodParameter parameter) throws BindException {
        if(parameter.hasParameterAnnotation(Validated.class) || parameter.hasParameterAnnotation(Valid.class)) {
            if(validator.supports(parameter.getParameterType())) {
                BeanPropertyBindingResult errors = new BeanPropertyBindingResult(arg, parameter.getParameterName());
                validator.validate(arg, errors);
                if(errors.hasErrors()) {
                    throw new BindException(errors);
                }
            }
        }
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
