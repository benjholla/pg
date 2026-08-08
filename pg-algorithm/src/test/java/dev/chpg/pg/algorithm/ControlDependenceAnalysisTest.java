package dev.chpg.pg.algorithm;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControlDependenceAnalysisTest {

    private Graph graph;
    private Node entry;
    private Node exit;

    @BeforeEach
    public void setup() {
        graph = new GlobalGraph();
    }

    @Test
    public void testNullEntryOrExit() {
        entry = graph.factory().createNode();
        exit = graph.factory().createNode();
        graph.addNode(entry);
        graph.addNode(exit);
        graph.addEdge(graph.factory().createEdge(entry, exit));

        assertThrows(IllegalArgumentException.class, () -> new ControlDependenceAnalysis(graph, null, exit));
        assertThrows(IllegalArgumentException.class, () -> new ControlDependenceAnalysis(graph, entry, null));
    }

    @Test
    public void testNoPathThrowsException() {
        entry = graph.factory().createNode();
        exit = graph.factory().createNode();
        graph.addNode(entry);
        graph.addNode(exit);

        assertThrows(IllegalArgumentException.class, () -> new ControlDependenceAnalysis(graph, entry, exit));
    }

    @Test
    public void testIfElseControlDependence() {
        // Constructing an if-else CFG:
        // entry -> condition
        // condition -> ifTrue (true branch)
        // condition -> ifFalse (false branch)
        // ifTrue -> exit
        // ifFalse -> exit

        entry = graph.factory().createNode();
        Node condition = graph.factory().createNode();
        Node ifTrue = graph.factory().createNode();
        Node ifFalse = graph.factory().createNode();
        exit = graph.factory().createNode();

        graph.addNode(entry);
        graph.addNode(condition);
        graph.addNode(ifTrue);
        graph.addNode(ifFalse);
        graph.addNode(exit);

        graph.addEdge(graph.factory().createEdge(entry, condition));
        graph.addEdge(graph.factory().createEdge(condition, ifTrue));
        graph.addEdge(graph.factory().createEdge(condition, ifFalse));
        graph.addEdge(graph.factory().createEdge(ifTrue, exit));
        graph.addEdge(graph.factory().createEdge(ifFalse, exit));

        ControlDependenceAnalysis analysis = new ControlDependenceAnalysis(graph, entry, exit);
        Map<Node, Set<Node>> dependencies = analysis.getControlDependencies();

        // Control dependence: ifTrue and ifFalse execute conditionally based on condition.
        // Therefore, ifTrue and ifFalse are control dependent on condition.
        assertTrue(dependencies.containsKey(condition));
        assertTrue(dependencies.get(condition).contains(ifTrue));
        assertTrue(dependencies.get(condition).contains(ifFalse));

        // Nodes strictly along the unconditional path (entry, condition, exit) shouldn't be dependent on anything here
        // that controls them directly besides implicit entry semantics if we were to model it.
        // FastDominanceAnalysis PDF properties state that if B is in PDF(A), then A is CD on B.
        // PDF of ifTrue in reverse CFG is condition. So ifTrue is CD on condition.
        assertTrue(!dependencies.getOrDefault(entry, Set.of()).contains(condition));
        assertTrue(!dependencies.getOrDefault(exit, Set.of()).contains(condition));
    }

    @Test
    public void testInjectEdges() {
        entry = graph.factory().createNode();
        Node condition = graph.factory().createNode();
        Node ifTrue = graph.factory().createNode();
        exit = graph.factory().createNode();

        graph.addNode(entry);
        graph.addNode(condition);
        graph.addNode(ifTrue);
        graph.addNode(exit);

        graph.addEdge(graph.factory().createEdge(entry, condition));
        graph.addEdge(graph.factory().createEdge(condition, ifTrue));
        graph.addEdge(graph.factory().createEdge(condition, exit));
        graph.addEdge(graph.factory().createEdge(ifTrue, exit));

        ControlDependenceAnalysis analysis = new ControlDependenceAnalysis(graph, entry, exit);
        analysis.injectEdges();

        // There should be a CD edge from condition to ifTrue
        boolean foundCDEdge = false;
        for (Edge e : graph.edges()) {
            if (e.tags().contains(ControlDependenceAnalysis.CONTROL_DEPENDENCE_EDGE)) {
                if (e.from().equals(condition) && e.to().equals(ifTrue)) {
                    foundCDEdge = true;
                }
            }
        }
        assertTrue(foundCDEdge, "Control dependence edge not injected correctly");
    }
}
