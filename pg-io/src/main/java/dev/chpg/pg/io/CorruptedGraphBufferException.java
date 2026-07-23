package dev.chpg.pg.io;

/**
 * Thrown when a DirectGraphBuffer (.dgb) file is missing its magic header/footer or is otherwise corrupted.
 */
public class CorruptedGraphBufferException extends RuntimeException {

    /**
     * undocumented.
     */
    private static final long serialVersionUID = 1L;

    /**
     * undocumented.
     */
    public CorruptedGraphBufferException(String message) {
        super(message);
    }

    /**
     * undocumented.
     */
    public CorruptedGraphBufferException(String message, Throwable cause) {
        super(message, cause);
    }
}
