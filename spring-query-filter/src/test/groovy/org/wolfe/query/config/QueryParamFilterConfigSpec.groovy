package org.wolfe.query.config

import org.springframework.core.convert.ConversionService
import org.springframework.validation.Validator
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.wolfe.query.pattern.DefaultQueryFilterPatternProvider
import org.wolfe.query.pattern.QueryFilterPatternProvider
import org.wolfe.query.support.QueryFilterHandlerMethodArgumentResolver
import org.wolfe.query.validator.QueryParamOperatorValidator
import spock.lang.Specification

class QueryParamFilterConfigSpec extends Specification {

    def "adds argument resolver"() {
        given:
        def resolver = Mock(QueryFilterHandlerMethodArgumentResolver)
        def config = new QueryParamFilterConfig()
        def list = new ArrayList<HandlerMethodArgumentResolver>()

        when:
        config.addArgumentResolvers(list)

        then:
        list.size() == 1
    }

    def "returns DefaultQueryFilterPatternProvider"() {
        given:
        def config = new QueryParamFilterConfig()

        when:
        def provider = config.queryFilterPatternProvider()

        then:
        provider instanceof DefaultQueryFilterPatternProvider
    }

    def "returns QueryParamOperatorValidtor"() {
        given:
        def config = new QueryParamFilterConfig()

        when:
        def constraint = config.queryParamOperatorValidator()

        then:
        constraint instanceof QueryParamOperatorValidator
    }

    def "return QueryFilterHandlerMethodArgumentResolver"() {
        given:
        def config = new QueryParamFilterConfig()
        def pattern = Mock(QueryFilterPatternProvider)
        def constraint = new QueryParamOperatorValidator()
        def conversion = Mock(ConversionService)
        def validator = Mock(Validator)

        when:
        def resolver = config.queryFilterHandlerMethodArgumentResolver(pattern, constraint, conversion, validator)

        then:
        resolver instanceof QueryFilterHandlerMethodArgumentResolver
    }
}
