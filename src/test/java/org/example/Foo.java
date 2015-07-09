package org.example;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author markford
 */
public class Foo {
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void valid() {
    }

    @PreAuthorize("hasRole(ROLE_ADMIN)")
    public void missingQuotes() {
        // is this an error?
    }

    @PreAuthorize("hasRoll('ROLE_ADMIN')")
    public void unknownFunction() {
        // is this an error?
    }
}
