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
        assertThrows(NullPointerException.class, () -> new HeavyNodeSet(new HeavyNode(), null));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new HeavyEdgeSet(new HeavyEdge(new HeavyNode(), new HeavyNode()), null));
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
        assertThrows(NullPointerException.class, () -> new HeavyGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> new HeavyGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> new HeavyGraph(java.util.Arrays.asList((Graph) null)));
    }
}
