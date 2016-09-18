package org.wolfe.query.web

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.WebApplicationContext
import org.wolfe.Application
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application)
class QueryFilterControllerSpec extends Specification {

    @Value('${local.server.port}')
    int port

    HTTPBuilder http

    @Autowired
    WebApplicationContext context

    def setup() {
        http = new HTTPBuilder("http://localhost:$port")
    }

    def "should boot up with no errors"() {
        expect: "web application context exists"
        context != null
    }

    def "standardQuery should return id"() {
        given:
        def filter = "'id=12345'"

        when:
        def (status, data) = http.get(path: "/query", query: [filter:filter]) {
            resp, reader -> [resp.status, reader]
        }

        then:
        status == HttpStatus.SC_OK
        data.id == "12345"
    }

    def "standardQuery should return all values"() {
        given:
        def filter = "'id=12345&email=bob@aol.com&balance>50'"

        when:
        def (status, data) = http.get(path: "/query", query: [filter:filter]) {
            resp, reader -> [resp.status, reader]
        }

        then:
        status == HttpStatus.SC_OK
        data.id == "12345"
        data.email == "bob@aol.com"
        data.balance == 50.0f
        data.balanceOperator == ">"
    }

    def "standardQueryWithValid should return a 400 Bad Request when id is null"() {
        given:
        def filter = "'email=bob@aol.com&balance>50'"

        when:
        int status
        http.handler.failure = {
            resp -> status = resp.status
        }
        http.get(path: "/valid", query: [filter:filter])

        then:
        status == HttpStatus.SC_BAD_REQUEST
    }

    def "standardQueryWithValidated should return a 400 Bad Request when id is null"() {
        given:
        def filter = "'email=bob@aol.com&balance>50'"

        when:
        int status
        http.handler.failure = {
            resp -> status = resp.status
        }
        http.get(path: "/validated", query: [filter:filter])

        then:
        status == HttpStatus.SC_BAD_REQUEST
    }

    def "standardQueryWithValid should return a 400 Bad Request when email is blank"() {
        given:
        def filter = "'id=12345&email=&balance>50'"

        when:
        int status
        http.handler.failure = {
            resp -> status = resp.status
        }
        http.get(path: "/valid", query: [filter:filter])

        then:
        status == HttpStatus.SC_BAD_REQUEST
    }

    def "standardQueryWithValid should return a 400 Bad Request when balance operator is invalid"() {
        given:
        def filter = "'id=12345&email=bob@aol.com&balance=50'"

        when:
        int status
        http.handler.failure = {
            resp -> status = resp.status
        }
        http.get(path: "/valid", query: [filter:filter])

        then:
        status == HttpStatus.SC_BAD_REQUEST
    }

    def "standardQueryWithValid should return a 400 Bad Request when email operator is invalid"() {
        given:
        def filter = "'id=12345&email>bob@aol.com&balance=50'"

        when:
        int status
        http.handler.failure = {
            resp -> status = resp.status
        }
        http.get(path: "/valid", query: [filter:filter])

        then:
        status == HttpStatus.SC_BAD_REQUEST
    }
}
