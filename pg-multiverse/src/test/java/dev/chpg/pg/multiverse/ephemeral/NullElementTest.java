package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.TagSet;

public class NullElementTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    @Test
    public void testNodeSetNull() {
        assertThrows(NullPointerException.class, () -> new EphemeralNodeSet(factory.createNode(), null));

        NodeSet nodeSet = new EphemeralNodeSet();
        assertThrows(NullPointerException.class, () -> nodeSet.add(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(java.util.Arrays.asList(factory.createNode(), null)));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new EphemeralEdgeSet(factory.createEdge(factory.createNode(), factory.createNode()), null));

        EdgeSet edgeSet = new EphemeralEdgeSet();
        assertThrows(NullPointerException.class, () -> edgeSet.add(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(java.util.Arrays.asList(factory.createEdge(factory.createNode(), factory.createNode()), null)));
    }


    @Test
    public void testTagSetNull() {
        TagSet tagSet = new EphemeralTagSet();
        assertThrows(NullPointerException.class, () -> tagSet.add(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(java.util.Arrays.asList("tag1", null)));
    }

    @Test
    public void testEphemeralGraphConstructorsNull() {
        assertThrows(NullPointerException.class, () -> factory.createGraph((Node) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((Edge) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((NodeSet) null, (EdgeSet) null));
    }
}
