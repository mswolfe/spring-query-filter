package org.wolfe.query.support

import org.springframework.core.MethodParameter
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.web.context.request.NativeWebRequest
import spock.lang.Shared
import spock.lang.Specification

class QueryFilterHandlerMethodArgumentResolverSpec extends Specification {

    @Shared
    def conversionService = new DefaultConversionService()

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

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

    def "Ensure setFilterProperty populates the value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

        when:
        def result = resolver.setFilterProperty(target, key, op, value)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

        when:
        def result = resolver.setFilterProperty(target, key, op, value)

        then:
        result == false
        target.name == null
    }

    def "Ensure setFilterOperatorProperty populates the value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

        when:
        def result = resolver.setFilterOperatorProperty(target, key, op, value)

        then:
        result == true
        target.nameOperator == "="
    }

    def "Ensure setFilterOperatorProperty fails to populate the value when the object is missing a setter"() {
        given:
        def target = new QueryFilterFieldObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

        when:
        def result = resolver.setFilterOperatorProperty(target, key, op, value)

        then:
        result == false
        target.nameOperator == null
    }

    def "Ensure setFilterOperatorProperty fails to populate the value when there is no property"() {
        given:
        def target = new QueryFilterNoOperatorObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

        when:
        def result = resolver.setFilterOperatorProperty(target, key, op, value)

        then:
        result == false
    }

    def "Ensure setMethod populates the value correctly"() {
        given:
        def target = new QueryFilterMethodObject()
        def key = "name"
        def op = "="
        def value = "bob"
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

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
        def resolver = new QueryFilterHandlerMethodArgumentResolver(conversionService)

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
}
