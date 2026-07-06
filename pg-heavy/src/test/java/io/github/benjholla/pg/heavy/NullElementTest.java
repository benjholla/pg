package io.github.benjholla.pg.heavy;

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
        assertThrows(NullPointerException.class, () -> new HeavyNodeSet((HeavyNode) new HeavyGraph().createNode(), null));

        NodeSet nodeSet = new HeavyNodeSet();
        assertThrows(NullPointerException.class, () -> nodeSet.add(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(java.util.Arrays.asList((HeavyNode) new HeavyGraph().createNode(), null)));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new HeavyEdgeSet((HeavyEdge) new HeavyGraph().createEdge((HeavyNode) new HeavyGraph().createNode(), (HeavyNode) new HeavyGraph().createNode()), null));

        EdgeSet edgeSet = new HeavyEdgeSet();
        assertThrows(NullPointerException.class, () -> edgeSet.add(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(java.util.Arrays.asList((HeavyEdge) new HeavyGraph().createEdge((HeavyNode) new HeavyGraph().createNode(), (HeavyNode) new HeavyGraph().createNode()), null)));
    }


    @Test
    public void testTagSetNull() {
        TagSet tagSet = new HeavyTagSet();
        assertThrows(NullPointerException.class, () -> tagSet.add(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(java.util.Arrays.asList("tag1", null)));
    }

    @Test
    public void testHeavyGraphConstructorsNull() {
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph().createGraph(java.util.Arrays.asList((Graph) null)));
    }
}
