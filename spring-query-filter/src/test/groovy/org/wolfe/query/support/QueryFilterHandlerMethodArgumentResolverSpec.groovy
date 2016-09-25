package org.wolfe.query.support

import org.apache.commons.logging.LogFactory
import org.springframework.core.MethodParameter
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.validation.BindException
import org.springframework.validation.Validator
import org.springframework.validation.annotation.Validated
import org.springframework.web.context.request.NativeWebRequest
import org.wolfe.query.QueryParamOperator
import org.wolfe.query.pattern.DefaultQueryFilterPatternProvider
import org.wolfe.query.validator.QueryParamOperatorValidator
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Valid
import java.lang.annotation.Annotation

class QueryFilterHandlerMethodArgumentResolverSpec extends Specification {

    @Shared
    def patternProvider = new DefaultQueryFilterPatternProvider();

    @Shared
    def conversionService = new DefaultConversionService()

    @Shared
    def queryValidator = new QueryParamOperatorValidator()

    @Shared
    def validator = Mock(Validator) {
        supports(_) >> true
    }

    def "Ensure resolveArgument splits on operator '#op' correctly"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterTestObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name"+op+"bob'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterTestObject
        def obj = (QueryFilterTestObject) result
        obj.nameOperator == op

        where:
        op | _
        ">" | _
        "<" | _
        "=" | _
        ">=" | _
        "<=" | _
    }

    def "Ensure resolveArgument splits on two operators of '#op' correctly"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterTestObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name"+op+"bob&email"+op+"fred@aol.com'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterTestObject
        def obj = (QueryFilterTestObject) result
        obj.nameOperator == op
        obj.emailOperator == op

        where:
        op | _
        ">" | _
        "<" | _
        "=" | _
        ">=" | _
        "<=" | _
    }

    def "Ensure resolveArgument splits filter 'name=bob' correctly"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterTestObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name=bob'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterTestObject
        def obj = (QueryFilterTestObject) result
        obj.name == "bob"
        obj.nameOperator == "="
    }

    def "Ensure resolveArgument splits filter 'name>=bob&email<sally@gmail.com' correctly"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterTestObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name>=bob&email<sally@gmail.com'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterTestObject
        def obj = (QueryFilterTestObject) result
        obj.name == "bob"
        obj.nameOperator == ">="
        obj.email == "sally@gmail.com"
        obj.emailOperator == "<"
    }

    def "Ensure resolveArgument splits filter 'name>=bob&email<sally@gmail.com&state=OR' correctly"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterTestObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name>=bob&email<sally@gmail.com&state=OR'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterTestObject
        def obj = (QueryFilterTestObject) result
        obj.name == "bob"
        obj.nameOperator == ">="
        obj.email == "sally@gmail.com"
        obj.emailOperator == "<"
        obj.state == "OR"
        obj.stateOperator == "="
    }

    def "Ensure resolveArgument returns a valid object when not populating comparison operator"() {
        given:
        def parameter = Mock(MethodParameter) {
            getParameterType() >> QueryFilterNoOperatorObject.class
        }
        def webRequest = Mock(NativeWebRequest) {
            getParameter(_ as String) >> "'name>=bob'"
        }
        def mavContainer = null
        def binderFactory = null
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        then:
        result instanceof QueryFilterNoOperatorObject
        def obj = (QueryFilterNoOperatorObject) result
        obj.name == "bob"
    }

    def "Ensure setFilterProperty populates the value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, value)

        then:
        result == true
        target.name == "bob"
    }

    def "Ensure setFilterProperty fails to populate the value when the object is missing a setter"() {
        given:
        def target = new QueryFilterFieldObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, value)

        then:
        result == false
        target.name == null
    }

    def "Ensure setFilterProperty populates the operator value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "nameOperator"
        def op = "="
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, op)

        then:
        result == true
        target.nameOperator == "="
    }

    def "Ensure setFilterProperty fails to populate the operator value when the object is missing a setter"() {
        given:
        def target = new QueryFilterFieldObject()
        def key = "nameOperator"
        def op = "="
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, op)

        then:
        result == false
        target.nameOperator == null
    }

    def "Ensure setFilterProperty fails to populate the value when there is no property"() {
        given:
        def target = new QueryFilterNoOperatorObject()
        def key = "nameOperator"
        def op = "="
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, op)

        then:
        result == false
    }

    def "Should supports valid argument"() {
        given:
        def parameter = Mock(MethodParameter) {
            hasParameterAnnotation(_) >> true
        }
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        def supports = resolver.supportsParameter(parameter)

        then:
        supports == true
    }

    def "should invoke validator when parameter is marked with @Valid annotation"() {
        given:
        def parameter = Mock(MethodParameter) {
            hasParameterAnnotation(_) >> true
            getParameterType() >> QueryFilterMethodObject.class

        }
        def validator = Mock(Validator) {
            supports(*_) >> true
        }
        def arg = new QueryFilterMethodObject()
        def filters = new HashMap<String, String>()
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        resolver.validateFilterArgument(arg, filters, parameter)

        then:
        1 * validator.validate(*_)
    }

    def "should invoke custom validation when object has @QueryParamOperator annotation"() {
        given:
        def parameter = Mock(MethodParameter) {
            hasParameterAnnotation(_) >> true
            getParameterType() >> QueryFilterValidationObject.class
            getParameterName() >> "name"
        }
        def validator = Mock(Validator) {
            supports(*_) >> false
        }
        def queryValidator = Mock(ConstraintValidator)
        def arg = new QueryFilterValidationObject()
        def filters = new HashMap<String, String>()
        filters.put("name", "bob")
        filters.put("nameOperator", "=")
        filters.put("email", "bob@aol.com")
        filters.put("emailOperator", "=")
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        resolver.validateFilterArgument(arg, filters, parameter)

        then:
        2 * queryValidator.initialize(*_)
        2 * queryValidator.isValid(*_) >> true
    }

    def "should correctly validate when object as @QueryParamOperator annotation"() {
        given:
        def parameter = Mock(MethodParameter) {
            hasParameterAnnotation(_) >> true
            getParameterType() >> QueryFilterValidationObject.class
            getParameterName() >> "name"
        }
        def validator = Mock(Validator) {
            supports(*_) >> false
        }
        def arg = new QueryFilterValidationObject()
        def filters = new HashMap<String, String>()
        filters.put("name", "bob")
        filters.put("nameOperator", "=")
        filters.put("email", "bob@aol.com")
        filters.put("emailOperator", ">")
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        resolver.validateFilterArgument(arg, filters, parameter)

        then:
        notThrown BindException
    }

    def "should correctly invalidate when object as @QueryParamOperator annotation"() {
        given:
        def parameter = Mock(MethodParameter) {
            hasParameterAnnotation(_) >> true
            getParameterType() >> QueryFilterValidationObject.class
            getParameterName() >> "name"
        }
        def validator = Mock(Validator) {
            supports(*_) >> false
        }
        def arg = new QueryFilterValidationObject()
        def filters = new HashMap<String, String>()
        filters.put("name", "bob")
        filters.put("nameOperator", ">")
        filters.put("email", "bob@aol.com")
        filters.put("emailOperator", "=")
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, queryValidator, conversionService, validator)

        when:
        resolver.validateFilterArgument(arg, filters, parameter)

        then:
        thrown BindException
    }
}

class QueryFilterFieldObject {
    public String name
    public String nameOperator
}

class QueryFilterMethodObject {
    String name
    String nameOperator
}

class QueryFilterNoOperatorObject {
    String name
}

class QueryFilterTestObject {
    String name
    String nameOperator
    String email
    String emailOperator
    String state
    String stateOperator
}

class QueryFilterValidationObject {
    @QueryParamOperator(allowed = "=")
    String name

    String email

    @QueryParamOperator(allowed = [">", "<"])
    String emailOperator
}
