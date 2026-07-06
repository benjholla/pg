package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.TagSet;

public class NullElementTest {

    @Test
    public void testNodeSetNull() {
        assertThrows(NullPointerException.class, () -> new EphemeralNodeSet((EphemeralNode) new EphemeralGraph().createNode(), null));

        NodeSet nodeSet = new EphemeralNodeSet();
        assertThrows(NullPointerException.class, () -> nodeSet.add(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(java.util.Arrays.asList((EphemeralNode) new EphemeralGraph().createNode(), null)));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new EphemeralEdgeSet((EphemeralEdge) new EphemeralGraph().createEdge((EphemeralNode) new EphemeralGraph().createNode(), (EphemeralNode) new EphemeralGraph().createNode()), null));

        EdgeSet edgeSet = new EphemeralEdgeSet();
        assertThrows(NullPointerException.class, () -> edgeSet.add(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(java.util.Arrays.asList((EphemeralEdge) new EphemeralGraph().createEdge((EphemeralNode) new EphemeralGraph().createNode(), (EphemeralNode) new EphemeralGraph().createNode()), null)));
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
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> new EphemeralGraph().createGraph(java.util.Arrays.asList((Graph) null)));
    }
}
