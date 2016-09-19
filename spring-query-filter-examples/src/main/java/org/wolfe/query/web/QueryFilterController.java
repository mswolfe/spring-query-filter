package org.wolfe.query.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.wolfe.query.QueryParamFilter;
import org.wolfe.query.model.QueryFilterRequestModel;

import javax.validation.Valid;

@RestController
@RequestMapping("")
public class QueryFilterController {

    /**
     * Mark your model object with the @QueryParamFilter annotation and the argument will be
     * resolved by parsing the 'filter' query parameter.
     *
     * @param filters
     * @return
     */
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public QueryFilterRequestModel standardQuery(@QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }

    /**
     * Marking the object with the @Valid annotation or @Validated annotation will ensure that standard
     * spring and JSR-303 validation is run on the resolved argument.
     *
     * @param filters
     * @return
     */
    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    public QueryFilterRequestModel standardQueryWithValid(@Valid @QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }

    /**
     * Marking the object with the @Valid annotation or @Validated annotation will ensure that standard
     * spring and JSR-303 validation is run on the resolved argument.
     *
     * @param filters
     * @return
     */
    @RequestMapping(value = "/validated", method = RequestMethod.GET)
    public QueryFilterRequestModel standardQueryWithValidated(@Validated @QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }
}
