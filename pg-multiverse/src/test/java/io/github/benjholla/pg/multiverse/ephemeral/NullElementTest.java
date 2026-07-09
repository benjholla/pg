package io.github.benjholla.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.TagSet;

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
        assertThrows(NullPointerException.class, () -> factory.createGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> factory.createGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> factory.createGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> factory.createGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> factory.createGraph(java.util.Arrays.asList((Graph) null)));
    }
}
