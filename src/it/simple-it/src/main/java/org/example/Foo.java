package org.example;

import org.springframework.security.access.prepost.PreAuthorize;

public interface Foo {
    @PreAuthorize("hasRole('foo')")
    public void callFoo();
}