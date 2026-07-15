package dev.chpg.pg.exporter;

public final class ExportAttributeValue {

    // The byte IDs must perfectly match your Java 17 pg-io reader specification
    public enum Type {
        STRING((byte) 0),
        BOOLEAN((byte) 1),
        INTEGER((byte) 2),
        LONG((byte) 3),
        DOUBLE((byte) 4),
        BYTE_ARRAY((byte) 5);

        public final byte marker;
        Type(byte marker) { this.marker = marker; }
    }

    private final Type type;
    private final Object value;

    private ExportAttributeValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() { return type; }
    public Object getValue() { return value; }

    // Static Factories (Provides a fluent, record-like feel for the adapter author)
    public static ExportAttributeValue ofString(String val) { return new ExportAttributeValue(Type.STRING, val); }
    public static ExportAttributeValue ofInt(int val) { return new ExportAttributeValue(Type.INTEGER, val); }
    public static ExportAttributeValue ofLong(long val) { return new ExportAttributeValue(Type.LONG, val); }
    public static ExportAttributeValue ofBoolean(boolean val) { return new ExportAttributeValue(Type.BOOLEAN, val); }
    public static ExportAttributeValue ofDouble(double val) { return new ExportAttributeValue(Type.DOUBLE, val); }
    public static ExportAttributeValue ofByteArray(byte[] val) { return new ExportAttributeValue(Type.BYTE_ARRAY, val); }
}
