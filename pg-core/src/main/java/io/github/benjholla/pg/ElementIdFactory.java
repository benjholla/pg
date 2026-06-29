package io.github.benjholla.pg;

import java.util.concurrent.atomic.AtomicInteger;

public enum ElementIdFactory {
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextAddress = new AtomicInteger(1);

    public ElementId create() {
        return new ElementId(nextAddress.getAndIncrement());
    }
}
