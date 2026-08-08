package dev.chpg.pg.evaluation;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.chpg.pg.api.AttributeValue;

public class SetAlgebraEquivalenceTest {

    private void runTest(java.util.function.Consumer<Graph> testLogic) {
        testLogic.accept(new GlobalGraph());

        Universe universe = new Universe();
        EphemeralGraph eGraph = new EphemeralGraph(universe);
        testLogic.accept(eGraph);
    }

    private Node createNode(Graph graph) {
        if (graph instanceof GlobalGraph gg) {
            return gg.createNode();
        } else if (graph instanceof EphemeralGraph eg) {
            return eg.factory().createNode();
        }
        throw new IllegalArgumentException("Unknown graph type");
    }

    private Edge createEdge(Graph graph, Node from, Node to) {
        if (graph instanceof GlobalGraph gg) {
            return gg.createEdge(from, to);
        } else if (graph instanceof EphemeralGraph eg) {
            return eg.factory().createEdge(from, to);
        }
        throw new IllegalArgumentException("Unknown graph type");
    }

    private Node findByWatermark(Graph graph, int lineage) {
        return graph.nodes().stream()
                .filter(n -> n.attributes().containsKey("test_lineage")
                        && ((AttributeValue.IntegerValue)n.attributes().get("test_lineage")).value() == lineage)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Node with lineage " + lineage + " not found"));
    }

    @Test
    public void testDifferenceEdges() {
        runTest(graph -> {
            Node a = createNode(graph);
            Node b = createNode(graph);
            Node c = createNode(graph);

            a.attributes().put("test_lineage", new AttributeValue.IntegerValue(1));
            b.attributes().put("test_lineage", new AttributeValue.IntegerValue(2));
            c.attributes().put("test_lineage", new AttributeValue.IntegerValue(3));

            Edge e1 = createEdge(graph, a, b);

            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addEdge(e1);

            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                Graph finalGraph = new EphemeralGraph(eg.universe());
                a = findByWatermark(finalGraph, 1);
                b = findByWatermark(finalGraph, 2);
                c = findByWatermark(finalGraph, 3);
                e1 = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            Graph diffEdges = graph.differenceEdges(e1);

            assertEquals(3, diffEdges.nodes().size(), "differenceEdges(Edge) must not drop the terminal nodes.");
            assertEquals(0, diffEdges.edges().size(), "differenceEdges(Edge) must drop the edge.");
            final Node fa = a;
            final Node fb = b;
            final Node fc = c;
            assertTrue(diffEdges.nodes().stream().anyMatch(n -> n.id() == fa.id()), "differenceEdges(Edge) missing node a");
            assertTrue(diffEdges.nodes().stream().anyMatch(n -> n.id() == fb.id()), "differenceEdges(Edge) missing node b");
            assertTrue(diffEdges.nodes().stream().anyMatch(n -> n.id() == fc.id()), "differenceEdges(Edge) missing node c");
        });
    }

    @Test
    public void testDifference() {
        runTest(graph -> {
            Node a = createNode(graph);
            Node b = createNode(graph);
            Node c = createNode(graph);

            a.attributes().put("test_lineage", new AttributeValue.IntegerValue(1));
            b.attributes().put("test_lineage", new AttributeValue.IntegerValue(2));
            c.attributes().put("test_lineage", new AttributeValue.IntegerValue(3));

            Edge e1 = createEdge(graph, a, b);

            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addEdge(e1);

            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                Graph finalGraph = new EphemeralGraph(eg.universe());
                a = findByWatermark(finalGraph, 1);
                b = findByWatermark(finalGraph, 2);
                c = findByWatermark(finalGraph, 3);
                e1 = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            Graph diff = graph.difference(e1);

            assertEquals(1, diff.nodes().size(), "difference(Edge) must remove both terminal nodes (leaving 1 out of 3)");
            assertEquals(0, diff.edges().size(), "difference(Edge) must remove the edge.");
            final Node fa = a;
            final Node fb = b;
            final Node fc = c;
            assertTrue(diff.nodes().stream().anyMatch(n -> n.id() == fc.id()), "difference(Edge) must retain the uninvolved node.");
            assertFalse(diff.nodes().stream().anyMatch(n -> n.id() == fa.id()), "difference(Edge) failed to remove terminal node a");
            assertFalse(diff.nodes().stream().anyMatch(n -> n.id() == fb.id()), "difference(Edge) failed to remove terminal node b");
        });
    }

    @Test
    public void testIntersection() {
        runTest(graph -> {
            Node a = createNode(graph);
            Node b = createNode(graph);
            Node c = createNode(graph);

            a.attributes().put("test_lineage", new AttributeValue.IntegerValue(1));
            b.attributes().put("test_lineage", new AttributeValue.IntegerValue(2));
            c.attributes().put("test_lineage", new AttributeValue.IntegerValue(3));

            Edge e1 = createEdge(graph, a, b);

            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addEdge(e1);

            if (graph instanceof EphemeralGraph eg) {
                eg.universe().promote(eg);
                Graph finalGraph = new EphemeralGraph(eg.universe());
                a = findByWatermark(finalGraph, 1);
                b = findByWatermark(finalGraph, 2);
                c = findByWatermark(finalGraph, 3);
                e1 = finalGraph.edges().iterator().next();
                graph = finalGraph;
            }

            Graph intersection = graph.intersection(e1);

            assertEquals(2, intersection.nodes().size(), "intersection(Edge) must retain both terminal nodes.");
            assertEquals(1, intersection.edges().size(), "intersection(Edge) must retain the edge.");
            final Node fa = a;
            final Node fb = b;
            final Edge fe1 = e1;
            assertTrue(intersection.nodes().stream().anyMatch(n -> n.id() == fa.id()), "intersection(Edge) missing node a");
            assertTrue(intersection.nodes().stream().anyMatch(n -> n.id() == fb.id()), "intersection(Edge) missing node b");
            assertTrue(intersection.edges().stream().anyMatch(e -> e.id() == fe1.id()), "intersection(Edge) missing edge e1");
        });
    }
}
