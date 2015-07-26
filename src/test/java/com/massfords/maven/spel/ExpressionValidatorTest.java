package com.massfords.maven.spel;

import org.junit.Test;
import org.springframework.expression.ParseException;
import org.springframework.security.access.expression.SecurityExpressionRoot;

/**
 * @author markford
 */
public class ExpressionValidatorTest {

    private ExpressionValidator validator = new ExpressionValidator();

    @Test
    public void ok() throws Exception {
        validator.validate("hasRole('ROLE_ADMIN')", SecurityExpressionRoot.class);
    }

    @Test(expected = ParseException.class)
    public void missingQuote() throws Exception {
        validator.validate("hasRole('ROLE_ADMIN)", SecurityExpressionRoot.class);
    }

    @Test(expected = ExpressionValidationException.class)
    public void unknownMethod() throws Exception {
        validator.validate("hasRoll('ROLE_ADMIN')", SecurityExpressionRoot.class);
    }
}
