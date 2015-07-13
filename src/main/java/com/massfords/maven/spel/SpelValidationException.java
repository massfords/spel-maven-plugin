package com.massfords.maven.spel;

/**
 * Thrown when there is a fatal error in validating Spel expressions
 * @author slazarus
 */
public class SpelValidationException extends Exception {

    public SpelValidationException(String message) {
        super(message);
    }

}
