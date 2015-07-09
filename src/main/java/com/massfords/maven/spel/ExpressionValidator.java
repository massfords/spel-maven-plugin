package com.massfords.maven.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author markford
 */
public class ExpressionValidator {
    private final ExpressionParser parser = new SpelExpressionParser();

    public boolean isValid(String expression) {
        parser.parseExpression(expression);
        return true;
    }

}
