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

    @Test
    public void varargs() throws Exception {
        validator.validate("hasAnyRole('ROLE_ADMIN', 'ROLE_FOO')", SecurityExpressionRoot.class);
    }

    @Test
    public void nonRootMethod() throws Exception {
        validator.validate("#foo.isOk()", SecurityExpressionRoot.class);
    }

    @Test
    public void beanReference() throws Exception {
        validator.validate("@foo.isOk()", SecurityExpressionRoot.class);
    }

    @Test
    public void otherType() throws Exception {
        validator.validate("T(foo.bar.Baz).isOk()", SecurityExpressionRoot.class);
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
