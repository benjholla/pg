package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShadowSetImmutabilityTest {

    private Universe universe;
    private EphemeralGraph graph;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        graph = new EphemeralGraph(universe);
    }

    @Test
    public void testShadowNodeSetMutationThrows() {
        Node n1 = graph.createNode();
        graph.addNode(n1);
        graph.flushPropertiesTo(universe); // To add it to universe.asGraph().nodes()
        ShadowNodeSet shadowNodeSet = new ShadowNodeSet(graph, universe.asGraph().nodes(), Collections.singleton(n1));

        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.add(n1));
        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.addAll(Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.remove(n1));
        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.removeAll(Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.retainAll(Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowNodeSet.clear());

        Iterator<Node> it = shadowNodeSet.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, () -> it.remove());

        // Also test array methods which are not explicitly failing but could be tested
        assertNotNull(shadowNodeSet.toArray());
        assertNotNull(shadowNodeSet.toArray(new Node[0]));
    }

    @Test
    public void testShadowEdgeSetMutationThrows() {
        Node n1 = graph.createNode();
        Node n2 = graph.createNode();
        Edge e1 = graph.createEdge(n1, n2);
        graph.addEdge(e1);
        graph.flushPropertiesTo(universe);

        ShadowEdgeSet shadowEdgeSet = new ShadowEdgeSet(graph, universe.asGraph().edges(), Collections.singleton(e1));

        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.add(e1));
        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.addAll(Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.remove(e1));
        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.removeAll(Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.retainAll(Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> shadowEdgeSet.clear());

        Iterator<Edge> it = shadowEdgeSet.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, () -> it.remove());

        assertNotNull(shadowEdgeSet.toArray());
        assertNotNull(shadowEdgeSet.toArray(new Edge[0]));
    }
}
