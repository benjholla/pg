package dev.chpg.pg.exporter;

/**
 * Represents a strongly-typed attribute value for export.
 * <p>
 * <b>What it represents:</b> A wrapper around a primitive or string value that explicitly tracks its data type for binary serialization.
 * <p>
 * <b>Why it exists:</b> To ensure that attribute types are strictly preserved when exporting graphs to the DirectGraphBuffer format.
 * <p>
 * <b>When to use it:</b> Use the static factory methods (e.g., {@code ofString}, {@code ofInt}) to wrap raw attribute values before attaching them to an {@link ExportNode} or {@link ExportEdge}.
 * <p>
 * <b>Thread safety:</b> This class is immutable and thread-safe.
 */
public final class ExportAttributeValue {

    /**
     * Enumerates the supported attribute data types and their binary markers.
     */
    // The byte IDs must perfectly match your Java 17 pg-io reader specification
    public enum Type {
        /** String type marker. */
        STRING((byte) 0),
        /** Boolean type marker. */
        BOOLEAN((byte) 1),
        /** Integer type marker. */
        INTEGER((byte) 2),
        /** Long type marker. */
        LONG((byte) 3),
        /** Double type marker. */
        DOUBLE((byte) 4),
        /** Byte array type marker. */
        BYTE_ARRAY((byte) 5);

        /** The binary type marker. */
        public final byte marker;
        Type(byte marker) { this.marker = marker; }
    }

    private final Type type;
    private final Object value;

    private ExportAttributeValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the type of this attribute value.
     *
     * @return the type
     */
    public Type getType() { return type; }

    /**
     * Returns the raw value payload.
     *
     * @return the value payload
     */
    public Object getValue() { return value; }

    // Static Factories (Provides a fluent, record-like feel for the adapter author)

    /**
     * Creates an export attribute value for a string.
     *
     * @param val the string value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofString(String val) { return new ExportAttributeValue(Type.STRING, val); }

    /**
     * Creates an export attribute value for an integer.
     *
     * @param val the integer value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofInt(int val) { return new ExportAttributeValue(Type.INTEGER, val); }

    /**
     * Creates an export attribute value for a long.
     *
     * @param val the long value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofLong(long val) { return new ExportAttributeValue(Type.LONG, val); }

    /**
     * Creates an export attribute value for a boolean.
     *
     * @param val the boolean value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofBoolean(boolean val) { return new ExportAttributeValue(Type.BOOLEAN, val); }

    /**
     * Creates an export attribute value for a double.
     *
     * @param val the double value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofDouble(double val) { return new ExportAttributeValue(Type.DOUBLE, val); }

    /**
     * Creates an export attribute value for a byte array.
     *
     * @param val the byte array value
     * @return a typed export attribute value
     */
    public static ExportAttributeValue ofByteArray(byte[] val) { return new ExportAttributeValue(Type.BYTE_ARRAY, val); }
}
