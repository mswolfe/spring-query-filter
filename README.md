# spring-query-filter

This library provides support for parsing and validating a 'filter' query parameter.

Have you every been in a situation where your front-end developer says something like: "Yo, Brian!  I tried passing a filter query parameter to the GET request in your service API and it didn't work...what's up with that man?"  Instantly, that small ball of fear and unabashed dread starts churning and burning deep in your belly as you say, "Filter query parameter...what is that?"

Well never fear!  The spring-query-filter library is here to make the stench of fear disappear!

## What is the 'filter' query parameter?

The 'filter' query parameter is a standard way to pass filter arguments to a GET request (or possibly any other request).

For example, if we want to request all customers from the '/api/customer' endpoint whose first name is 'sally' we could invoke this:

```
/api/customer?filter='firstName=sally'
```

Great, but now we want all customers with a first name of 'sally' and an age greater than 25:

```
/api/customer?filter='firstName=sally&age>25'
```

Excellent, but now we want all customers with a first name of 'sally', an age greater than 25 and a weight less than 120lbs:

```
/api/customer?filter='firstName=sally&age>25&weight<=120
```

You get the idea!

## Supported Features

* Parses a multi-parameter 'filter' query parameter
* Resolves a controller method argument with the parsed values
* Perform standard spring-framework and JSR-303 validation
* Easy Spring annotation based integration

## It's Simple to Use!

Download the jar though [Maven Central](https://oss.sonatype.org/service/local/staging/deploy/maven2/com/github/mswolfe/spring-query-filter):

or pull in into your maven project: 
```xml
<dependency>
  <groupId>com.github.mswolfe</groupId>
  <artifactId>spring-query-filter</artifactId>
  <version>4.3.0</version>
</dependency>
```

or even via gradle:
```groovy
compile 'org.github.mswolfe:spring-query-filter:4.3.0'
```

## Create a POJO

Create a regular old POJO that has getters and setters for each supported filter argument.

```java
public class QueryFilterRequestModel {

    @NotNull
    private String id;

    @QueryParamOperator(allowed = "=")
    private String email;

    /** Getters and Setters to make this a POJO. */
}
```

## Create a Controller

Create a controller with a request method that accepts the POJO and annotate it with the @QueryFilterParameter annotation.

```java
@RestController
@RequestMapping("")
public class QueryFilterController {

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public QueryFilterRequestModel standardQuery(@QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }
}
```

And if you're feeling saucy you can annotate the POJO with the @Valid annotation to enable validation on your POJO.

See the spring-query-filter-examples project for full code examples on how to use this library.

## Version & Spring Framework compatibility ##

The major and minor number of this library refers to the compatible Spring framework version. The build number is used as specified by SEMVER.

API changes will follow SEMVER and loosly the Spring Framework releases.

| `spring-query-filter` version  | Spring Framework compatibility |
| ------------- | ------------- |
| 4.2.x  | >= 4.2 |
| 4.3.x  | >= 4.3 |
