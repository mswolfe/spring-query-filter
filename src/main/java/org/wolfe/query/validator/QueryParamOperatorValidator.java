package org.wolfe.query.validator;

import com.sun.istack.internal.NotNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.wolfe.query.QueryParamOperator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class QueryParamOperatorValidator implements ConstraintValidator<QueryParamOperator, String> {

    private String[] allowedParams;

    @Override
    public void initialize(QueryParamOperator constraintAnnotation) {
        allowedParams = constraintAnnotation.allowed();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(!StringUtils.isEmpty(value)) {
            for (String ap : allowedParams) {
                if (value.equals(ap)) {
                    return true;
                }
            }
        }

        return false;
    }
}
