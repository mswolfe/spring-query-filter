package org.wolfe.query.pattern;

import java.util.regex.Pattern;

public interface QueryFilterPatternProvider {
    /**
     * @return Returns the pattern used to split and match the filter parameter argument.
     */
    Pattern getPattern();

    /**
     *
     * @return Returns the delimiter that separates each filter parameter.
     */
    String getParameterDelimiter();

    /**
     * @return Returns the name of the parameter to use during argument resolution.
     */
    String getParameterName();
}
