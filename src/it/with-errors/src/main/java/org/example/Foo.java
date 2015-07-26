package org.example;

import org.springframework.security.access.prepost.PreAuthorize;

public interface Foo {
    @PreAuthorize("hasRoll('foo')")
    public void badMethod();

    @PreAuthorize("hasRoll('foo)")
    public void unterminatedString();
}