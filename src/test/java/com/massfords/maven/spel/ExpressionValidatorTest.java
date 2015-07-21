package com.massfords.maven.spel;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.expression.ParseException;

/**
 * @author markford
 */
public class ExpressionValidatorTest {

    private ExpressionValidator validator = new ExpressionValidator();

    @Test
    public void ok() throws Exception {
        validator.validate("hasRole('ROLE_ADMIN')");
    }

    @Test(expected = ParseException.class)
    public void missingQuote() throws Exception {
        validator.validate("hasRole('ROLE_ADMIN)");
    }

    @Test(expected = ExpressionValidationException.class)
    @Ignore("disabled until we integrate a contexts for expressions")
    public void unknownFunction() throws Exception {
        validator.validate("hasRoll('ROLE_ADMIN')");
    }
}
