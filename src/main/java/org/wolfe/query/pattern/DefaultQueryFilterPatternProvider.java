package org.wolfe.query.pattern;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DefaultQueryFilterPatternProvider implements QueryFilterPatternProvider {

    // Regex is: (.+?)(>=|<=|=|<|>)(.+?)
    private static final String FILTER_KEY_REGEX = "(.+?)(";
    private static final String FILTER_COMPARISON_REGEX = ">=|<=|=|<|>";
    private static final String FILTER_VALUE_REGEX = ")(.+?)";

    private static final String FILTER_PARAMETER_DELIMITER = "&";

    private final Pattern FILTER_PATTERN;

    public DefaultQueryFilterPatternProvider() {
        FILTER_PATTERN = Pattern.compile(getFilterRegex());
    }

    @Override
    public Pattern getPattern() {
        return FILTER_PATTERN;
    }

    @Override
    public String getParameterDelimiter() {
        return FILTER_PARAMETER_DELIMITER;
    }

    public String getFilterRegex() {
        return FILTER_KEY_REGEX + getFilterComparisonRegex() + FILTER_VALUE_REGEX;
    }

    public String getFilterComparisonRegex() {
        return FILTER_COMPARISON_REGEX;
    }
}
