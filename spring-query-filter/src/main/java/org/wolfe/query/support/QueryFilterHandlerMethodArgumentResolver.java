package org.wolfe.query.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.wolfe.query.QueryParamFilter;
import org.wolfe.query.QueryParamOperator;
import org.wolfe.query.pattern.QueryFilterPatternProvider;
import org.wolfe.query.validator.QueryParamOperatorValidator;

import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QueryFilterHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final String filterParameterName;
    private final Pattern filterPattern;
    private final String filterParameterDelimiter;

    private final ConversionService conversionService;
    private final Validator validator;

    private static final Log log = LogFactory.getLog(QueryFilterHandlerMethodArgumentResolver.class);

    @Autowired
    public QueryFilterHandlerMethodArgumentResolver(QueryFilterPatternProvider queryFilterPatternProvider,
                                                    ConversionService conversionService,
                                                    @Qualifier("mvcValidator") Validator validator) {
        this.filterPattern = queryFilterPatternProvider.getPattern();
        this.filterParameterDelimiter = queryFilterPatternProvider.getParameterDelimiter();
        this.filterParameterName = queryFilterPatternProvider.getParameterName();

        this.conversionService = conversionService;
        this.validator = validator;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(QueryParamFilter.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object arg = parameter.getParameterType().newInstance();
        String filter = webRequest.getParameter(filterParameterName);

        // Parse the created object with the values from the filter argument.
        Map<String, String> filterArguments = parseFilterArgument(filter);

        // Populate the object with data from the parsed argument.
        populateArgumentProperties(arg, filterArguments);

        // Perform validation on the created object.
        validateFilterArgument(arg, filterArguments, parameter);

        return arg;
    }

    /**
     * 1. Use the filter query parameter and strip the apostrophe from the start and end
     * 2. Split the string based upon the parameter delimiter
     * 3. Use the pattern regex to match each parameter, "<key><op><value>"
     * 4. Store the key, operator, and value in a map object:
     * key => value
     * key+"Operator" => operator
     *
     * @param filter
     * @return A mapping of key to value and <key>Operator to operator value.
     */
    private Map<String, String> parseFilterArgument(String filter) {
        Map<String, String> properties = new HashMap<>();

        if (!StringUtils.isEmpty(filter) && filter.length() > 2 && filter.startsWith("'") && filter.endsWith("'")) {
            String withOutParens = filter.substring(1, filter.length() - 1);
            String[] params = withOutParens.split(filterParameterDelimiter);
            if (log.isInfoEnabled()) {
                log.info(String.format("Found %s filter query parameters", params.length));
            }

            for (String param : params) {
                Matcher matcher = filterPattern.matcher(param);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    String key = matcher.group(1);
                    String op = matcher.group(2);
                    String value = matcher.group(3);

                    properties.put(key, value);
                    properties.put(key + "Operator", op);
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn(String.format("Failed to parse filter query parameter '%s'", param));
                    }
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Failed to trim apostrophes from the filter '%s'", filter));
            }
        }

        return properties;
    }

    /**
     * Invoke setFilterProperty for each item in the filters map on the given target Object arg.
     *
     * @param arg
     * @param filters
     */
    private void populateArgumentProperties(Object arg, Map<String, String> filters) {
        for (String key : filters.keySet()) {
            String value = filters.get(key);
            setFilterProperty(arg, key, value);
        }
    }

    /**
     * Validates the given object if the method parameter has been annotated with either the
     *
     * @param arg
     * @param filters
     * @param parameter
     * @throws BindException
     * @Valid or @Validated annotations using the built in validator.
     * <p>
     * Also, performs custom validation on the QueryParamOperator when it is annotating the key
     * or operator property.
     */
    private void validateFilterArgument(Object arg, Map<String, String> filters, MethodParameter parameter) throws BindException {
        if (parameter.hasParameterAnnotation(Validated.class) || parameter.hasParameterAnnotation(Valid.class)) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(arg, parameter.getParameterName());

            if (validator.supports(parameter.getParameterType())) {
                validator.validate(arg, errors);

            }

            // Perform custom annotation validation
            QueryParamOperatorValidator queryParamOperatorValidator = new QueryParamOperatorValidator();
            for (Field f : parameter.getParameterType().getDeclaredFields()) {
                for (Annotation a : f.getAnnotations()) {
                    if (a instanceof QueryParamOperator) {
                        QueryParamOperator op = (QueryParamOperator) a;

                        String name = f.getName();
                        String opName = name.endsWith("Operator") ? name : name + "Operator";
                        if (filters.containsKey(opName)) {
                            String value = filters.get(opName);

                            queryParamOperatorValidator.initialize(op);
                            if (!queryParamOperatorValidator.isValid(value, null)) {
                                errors.addError(new FieldError(parameter.getParameterName(), name, op.message()));
                            }
                        }
                    }
                }
            }

            if (errors.hasErrors()) {
                throw new BindException(errors);
            }
        }
    }

    /**
     * Sets the filter property for the target Object with the property name of key.  This will only invoke
     * the setter method on a POJO and will not inject into a field.
     *
     * @param target
     * @param key
     * @param value
     * @return
     */
    private boolean setFilterProperty(Object target, String key, String value) {
        if (setBeanProperty(target, key, value)) {
            return true;
        } else {
            if (log.isErrorEnabled()) {
                log.error(String.format("Failed to set the filter property for '%s=%s'", key, value));
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
        if (accessor != null && accessor.isWritableProperty(name)) {
            Class paramType = accessor.getPropertyType(name);
            if (paramType != null) {
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
                    if (log.isWarnEnabled()) {
                        log.warn(String.format("ConversionService cannot convert filter parameter '%s' to type '%s'", name, paramType.getName()));
                    }
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error(String.format("Failed to invoke setter method for '%s'; could not determine parameter type.", name));
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Property '%s' is not writable", name));
            }
        }

        return false;
    }
}
