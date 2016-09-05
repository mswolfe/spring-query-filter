package org.wolfe.query.pattern;

import java.util.regex.Pattern;

public interface QueryFilterPatternProvider {
    /**
     * Returns the pattern used to split and match the filter parameter argument.
     *
     * @return
     */
    Pattern getPattern();

    /**
     * Returns the delimiter that separates each filter parameter.
     *
     * @return
     */
    String getParameterDelimiter();

    /**
     * Returns the name of the parameter to use during argument resolution.
     *
     * @return
     */
    String getParameterName();
}
