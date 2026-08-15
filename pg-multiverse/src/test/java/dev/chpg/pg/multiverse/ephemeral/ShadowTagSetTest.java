package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ShadowTagSetTest {

    private Universe universe;
    private EphemeralGraph graph;
    private Node n1;
    private EphemeralFactory factory;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        graph = new EphemeralGraph(universe);
        factory = graph.factory();

        n1 = factory.createNode();
        graph.addNode(n1);

        n1.tags().add("tag1");
        n1.tags().add("tag2");
    }

    @Test
    public void testShadowTagSetMethods() {
        TagSet tags = n1.tags(); // returns ShadowTagSet internally when fetched

        assertEquals(2, tags.size());
        assertTrue(tags.contains("tag1"));

        Iterator<String> iter = tags.iterator();
        assertTrue(iter.hasNext());
        String t = iter.next();
        assertTrue(t.equals("tag1") || t.equals("tag2"));
    }
}
