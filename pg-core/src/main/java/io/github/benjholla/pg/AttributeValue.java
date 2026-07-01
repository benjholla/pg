package io.github.benjholla.pg;

import java.util.Arrays;

public sealed interface AttributeValue permits
    AttributeValue.StringVal,
    AttributeValue.IntVal,
    AttributeValue.LongVal,
    AttributeValue.FloatVal,
    AttributeValue.DoubleVal,
    AttributeValue.BooleanVal,
    AttributeValue.ByteArrayVal {

    // 1. Strings
    record StringVal(String value) implements AttributeValue {}

    // 2. Primitives
    record IntVal(int value) implements AttributeValue {}
    record LongVal(long value) implements AttributeValue {}
    record FloatVal(double value) implements AttributeValue {}
    record DoubleVal(double value) implements AttributeValue {}
    record BooleanVal(boolean value) implements AttributeValue {}

    // 3. The Escape Hatch
    record ByteArrayVal(byte[] value) implements AttributeValue {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof ByteArrayVal that) {
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
