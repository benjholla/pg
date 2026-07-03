package io.github.benjholla.pg.universe.ephemeral;

import java.util.concurrent.atomic.AtomicInteger;

public enum EphemeralIdGenerator {
    INSTANCE;

    // reserving zero and positive values for universe elements
    private AtomicInteger nextAddress = new AtomicInteger(-1);

    public int create() {
        return nextAddress.getAndDecrement();
    }
}
