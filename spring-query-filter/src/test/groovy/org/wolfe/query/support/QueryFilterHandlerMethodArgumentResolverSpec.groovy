package org.wolfe.query.support

import org.springframework.core.MethodParameter
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.validation.Validator
import org.springframework.web.context.request.NativeWebRequest
import org.wolfe.query.pattern.DefaultQueryFilterPatternProvider
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class QueryFilterHandlerMethodArgumentResolverSpec extends Specification {

    @Shared
    def patternProvider = new DefaultQueryFilterPatternProvider();

    @Shared
    def conversionService = new DefaultConversionService()

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

        when:
        def result = resolver.setFilterProperty(target, key, op)

        then:
        result == false
    }

    def "Ensure setMethod populates the value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

        when:
        def result = resolver.setBeanProperty(target, key, value)

        then:
        result == true
        target.name == "bob"
    }

    def "Ensure setMethod fails to populate the value when the object is missing a setter"() {
        given:
        def target = new QueryFilterFieldObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(patternProvider, conversionService, validator)

        when:
        def result = resolver.setBeanProperty(target, key, value)

        then:
        result == false
        target.name == null
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
