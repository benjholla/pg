package io.github.benjholla.pg.io;

/**
 * Thrown when a DirectGraphBuffer (.dgb) file is missing its magic header/footer or is otherwise corrupted.
 */
public class CorruptedGraphBufferException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CorruptedGraphBufferException(String message) {
        super(message);
    }

    public CorruptedGraphBufferException(String message, Throwable cause) {
        super(message, cause);
    }
}
