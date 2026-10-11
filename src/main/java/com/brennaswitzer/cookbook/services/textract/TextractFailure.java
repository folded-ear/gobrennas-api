package com.brennaswitzer.cookbook.services.textract;

import lombok.Getter;

/**
 * I am a failure to extract a photo's text, and whether trying again, with
 * nothing changed, can plausibly succeed.
 */
@Getter
public class TextractFailure extends RuntimeException {

    public static final String UNKNOWN_ERROR = "Unknown error.";

    private final boolean retryable;

    public TextractFailure(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

}
