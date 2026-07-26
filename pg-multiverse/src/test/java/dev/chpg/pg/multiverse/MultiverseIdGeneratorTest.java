package dev.chpg.pg.multiverse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MultiverseIdGeneratorTest {

    @Test
    public void testSequentialIdGeneration() {
        int id1 = MultiverseIdGenerator.INSTANCE.createUniverseId();
        int id2 = MultiverseIdGenerator.INSTANCE.createUniverseId();

        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertEquals(id1 + 1, id2);
    }
}
