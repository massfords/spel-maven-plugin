package com.massfords.maven.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;


/**
 * Validator for the expressions. Will eventually do more than just parse the
 * expression, but for now, that's all it does.
 *
 * Additional behavior in the works:
 * - validate method calls
 * - validate function calls
 *
 * @author markford
 */
public class ExpressionValidator {
    private final ExpressionParser parser = new SpelExpressionParser();

    public void validate(String expression) throws ExpressionValidationException, ParseException {
        parser.parseExpression(expression);
    }
}
