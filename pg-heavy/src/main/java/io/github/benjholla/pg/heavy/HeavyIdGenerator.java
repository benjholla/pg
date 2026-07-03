package io.github.benjholla.pg.heavy;

import java.util.concurrent.atomic.AtomicInteger;

public enum HeavyIdGenerator {
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextAddress = new AtomicInteger(1);

    public int create() {
        return nextAddress.getAndIncrement();
    }
}
