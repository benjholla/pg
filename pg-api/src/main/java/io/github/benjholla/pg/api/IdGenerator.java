package io.github.benjholla.pg.api;

import java.util.concurrent.atomic.AtomicInteger;

public enum IdGenerator {
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextAddress = new AtomicInteger(1);

    public int create() {
        return nextAddress.getAndIncrement();
    }
}
