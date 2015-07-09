package com.massfords.maven.spel;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author markford
 */
@Ignore
public class ExpressionValidatorTest {

    private ExpressionValidator validator = new ExpressionValidator();

    @Test
    public void ok() throws Exception {
        assertTrue(validator.isValid("hasRole('ROLE_ADMIN')"));
    }

    @Test
    public void missingQuote() throws Exception {
        assertFalse(validator.isValid("hasRole('ROLE_ADMIN)"));
    }

    @Test
    public void unknownFunction() throws Exception {
        assertFalse(validator.isValid("hasRoll('ROLE_ADMIN')"));
    }
}
