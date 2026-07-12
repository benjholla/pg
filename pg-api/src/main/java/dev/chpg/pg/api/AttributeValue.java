package dev.chpg.pg.api;

import java.util.Arrays;

/**
 * Represents a strictly typed, memory-safe property value within the graph ecosystem.
 * <p>
 * This sealed interface purposefully restricts graph attributes to a highly curated 
 * subset of Java primitives and core types: {@code String}, {@code boolean}, {@code int}, 
 * {@code long}, {@code double}, and {@code byte[]}.
 * <p>
 * <b>Architectural Rationale for Subset Selection:</b>
 * <ul>
 * <li><b>Heap Optimization (The Object Padding Fallacy):</b> Because these values must be 
 * wrapped in objects (records) to satisfy the interface, they are subject to the JVM's 
 * 8-byte memory alignment. A hypothetical {@code ShortValue} or {@code ByteValue} record
 * would consume the exact same amount of heap space (typically 24 bytes) as an 
 * {@code IntegerValue}. Excluding smaller primitives prevents API bloat without sacrificing
 * memory efficiency.</li>
 * <li><b>Pattern Matching Simplicity:</b> Restricting the number of permitted implementations 
 * drastically reduces the exhaustive branch requirements for downstream consumers. When 
 * extracting properties via Java {@code switch} expressions, developers only need to handle 
 * a maximum of 6 branches rather than the full suite of Java primitives.</li>
 * <li><b>JSON &amp; Transport Interoperability:</b> This specific subset perfectly mirrors
 * native JSON data types (String, Number, Boolean). This guarantees a frictionless, 
 * 1-to-1 serialization pipeline in the {@code pg-io} module, ensuring attributes map cleanly 
 * to the {@code pgv} TypeScript visualizer.</li>
 * </ul>
 * <p>
 * <b>The Character Exclusion (Why {@code StringValue} instead of {@code CharValue}?):</b>
 * <ul>
 * <li><b>The Surrogate Pair Safety Net:</b> A single 16-bit {@code char} cannot physically represent 
 * many modern Unicode characters (e.g., emojis, complex mathematical symbols), which require 
 * 32 bits (a surrogate pair). Forcing text through {@code StringValue} guarantees that multi-byte
 * characters are preserved perfectly without silent data truncation.</li>
 * <li><b>Encoding Translation Delegation:</b> Java holds text in memory as UTF-16, but external 
 * systems (JSON, web viewers) expect UTF-8. By requiring {@code String}, the library delegates 
 * the complex byte-shifting of UTF-8 translation entirely to the standard JDK libraries during 
 * serialization.</li>
 * <li><b>Compact Strings (JDK 9+):</b> Modern JVMs optimize {@code String} to be backed by a 
 * {@code byte[]} rather than a {@code char[]}. For standard ASCII text, a {@code StringValue} is
 * actually more memory-efficient per character than forcing the JVM to allocate a 16-bit 
 * {@code char} wrapper and bypass its native string optimizations.</li>
 * </ul>
 * * @implNote For complex application objects, standard 32-bit floats, single characters, or 
 * external schemas that do not fit natively into this subset, consumers must map them to 
 * standard types (e.g., passing a {@code char} as a 1-character {@code String}) or serialize 
 * them and utilize the {@code ByteArrayValue} escape hatch.
 */
public sealed interface AttributeValue permits
    AttributeValue.StringValue,
    AttributeValue.BooleanValue,
    AttributeValue.IntegerValue,
    AttributeValue.LongValue,
    AttributeValue.DoubleValue,
    AttributeValue.ByteArrayValue {


    static StringValue value(String value) { return new StringValue(value); }
    static BooleanValue value(boolean value) { return new BooleanValue(value); }
    static IntegerValue value(int value) { return new IntegerValue(value); }
    static LongValue value(long value) { return new LongValue(value); }
    static DoubleValue value(double value) { return new DoubleValue(value); }
    static ByteArrayValue value(byte[] value) { return new ByteArrayValue(value); }

    // 1. Strings
    record StringValue(String value) implements AttributeValue {}

    // 2. Primitives
    record BooleanValue(boolean value) implements AttributeValue {}
    record IntegerValue(int value) implements AttributeValue {}
    record LongValue(long value) implements AttributeValue {}
    record DoubleValue(double value) implements AttributeValue {}

    // 3. The Escape Hatch
    record ByteArrayValue(byte[] value) implements AttributeValue {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof ByteArrayValue that) {
                return Arrays.equals(this.value, that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }
}
