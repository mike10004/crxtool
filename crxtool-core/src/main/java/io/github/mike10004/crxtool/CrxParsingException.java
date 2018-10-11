package io.github.mike10004.crxtool;

import java.io.IOException;

/**
 * Exception thrown if parsing a CRX fails.
 */
@SuppressWarnings("unused")
public class CrxParsingException extends IOException {

    /**
     * Constructs an instance.
     * @param message the message
     */
    public CrxParsingException(String message) {
        super(message);
    }

    /**
     * Constructs an instance.
     * @param message the message
     * @param cause the cause
     */
    public CrxParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance.
     * @param cause the cause
     */
    public CrxParsingException(Throwable cause) {
        super(cause);
    }

}
