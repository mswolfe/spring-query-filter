package org.wolfe.query.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.wolfe.query.QueryParamFilter;
import org.wolfe.query.model.QueryFilterRequestModel;

import javax.validation.Valid;

@RestController
@RequestMapping("")
public class QueryFilterController {

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = "application/json")
    public QueryFilterRequestModel standardQuery(@QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = "application/json")
    public QueryFilterRequestModel standardQueryWithValidation(@Valid @QueryParamFilter QueryFilterRequestModel filters) {
        return filters;
    }
}
