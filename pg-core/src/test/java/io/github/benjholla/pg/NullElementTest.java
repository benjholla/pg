package io.github.benjholla.pg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NullElementTest {

    @Test
    public void testNodeSetNull() {
        assertThrows(NullPointerException.class, () -> new NodeSet(new Node(), null));
    }

    @Test
    public void testEdgeSetNull() {
        assertThrows(NullPointerException.class, () -> new EdgeSet(new Edge(new Node(), new Node()), null));
    }


    @Test
    public void testTagSetNull() {
        TagSet tagSet = new TagSet();
        assertThrows(NullPointerException.class, () -> tagSet.add(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(null));
        assertThrows(NullPointerException.class, () -> tagSet.addAll(java.util.Arrays.asList("tag1", null)));
    }

    @Test
    public void testPropertyGraphConstructorsNull() {
        assertThrows(NullPointerException.class, () -> new PropertyGraph((Node[]) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph(new Node[]{null}));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((NodeSet) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((Edge[]) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph(new Edge[]{null}));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((NodeSet) null, (EdgeSet) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((Graph[]) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph(new Graph[]{null}));
        assertThrows(NullPointerException.class, () -> new PropertyGraph((java.util.Collection<Graph>) null));
        assertThrows(NullPointerException.class, () -> new PropertyGraph(java.util.Arrays.asList((Graph) null)));
    }
}
