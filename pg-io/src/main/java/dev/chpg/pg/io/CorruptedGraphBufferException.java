package dev.chpg.pg.io;

/**
 * Thrown when a DirectGraphBuffer (.dgb) file is missing its magic header/footer or is otherwise corrupted.
 */
public class CorruptedGraphBufferException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new CorruptedGraphBufferException with the specified detail message.
     *
     * @param message the detail message
     */
    public CorruptedGraphBufferException(String message) {
        super(message);
    }

    /**
     * Constructs a new CorruptedGraphBufferException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CorruptedGraphBufferException(String message, Throwable cause) {
        super(message, cause);
    }
}
