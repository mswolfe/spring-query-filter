package org.wolfe.query;

import org.wolfe.query.validator.QueryParamOperatorValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QueryParamOperatorValidator.class)
public @interface QueryParamOperator {

    String message() default "operator not supported";

    String[] allowed() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
