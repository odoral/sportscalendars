package org.doral.sportcalendars.webscraper.util.htmlunit;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.html.HtmlElement;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author odoral
 */
public class HTMLUnitUtils {

    private HTMLUnitUtils() {
    }

    public static Optional<String> extractTextByFirstXpath(HtmlElement element, String xpathExpr) {
        return Optional.ofNullable(element.getFirstByXPath(xpathExpr))
                .filter(HtmlElement.class::isInstance)
                .map(HtmlElement.class::cast)
                .map(HtmlElement::getTextContent)
                .map(StringUtils::trimToNull);
    }

    public static boolean attributeContainsValue(HtmlElement element, String attributeName, String value) {
        String attributeValue = element.getAttribute(attributeName);
        return Arrays.asList(attributeValue.split("\\s+")).contains(value);
    }

}
