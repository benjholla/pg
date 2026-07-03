package io.github.benjholla.pg.universe.ephemeral;

import java.util.concurrent.atomic.AtomicInteger;

public enum EphemeralIdGenerator {
    INSTANCE;

    // zero is reserved and positive values are reserved for universe elements
    private AtomicInteger nextAddress = new AtomicInteger(-1);

    public int create() {
        return nextAddress.getAndDecrement();
    }
}
