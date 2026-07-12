package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.TagSet;

public class NullElementTest {

    @Test
    public void testNodeSetNull() {
        assertThrows(NullPointerException.class, () -> new GlobalNodeSet(new GlobalNode(), null));

        NodeSet nodeSet = new GlobalNodeSet();
        assertThrows(NullPointerException.class, () -> nodeSet.add(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> nodeSet.addAll(java.util.Arrays.asList(new GlobalNode(), null)));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new GlobalEdgeSet(new GlobalEdge(new GlobalNode(), new GlobalNode()), null));

        EdgeSet edgeSet = new GlobalEdgeSet();
        assertThrows(NullPointerException.class, () -> edgeSet.add(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(null));
        assertThrows(NullPointerException.class, () -> edgeSet.addAll(java.util.Arrays.asList(new GlobalEdge(new GlobalNode(), new GlobalNode()), null)));
    }


    @Test
    public void testTagSetNull() {
        TagSet tagSet = new GlobalTagSet();
        assertThrows(NullPointerException.class, () -> tagSet.add(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(java.util.Arrays.asList("tag1", null)));
    }

    @Test
    public void testGlobalGraphConstructorsNull() {
        assertThrows(NullPointerException.class, () -> new GlobalGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> new GlobalGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> new GlobalGraph(java.util.Arrays.asList((Graph) null)));
    }
}
