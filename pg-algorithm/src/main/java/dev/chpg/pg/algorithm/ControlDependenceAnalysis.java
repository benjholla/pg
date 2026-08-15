package dev.chpg.pg.algorithm;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes the Control Dependence of a graph.
 *
 * Node A is control-dependent on Node B if B determines whether A executes.
 * Mathematically, this is computed by finding the Post-Dominance Frontier (PDF)
 * of the graph and reversing the edges: if B is in PDF(A), then A is control-dependent on B.
 */
public class ControlDependenceAnalysis {

    /**
     * The tag applied to control dependence edges.
     */
    public static final String CONTROL_DEPENDENCE_EDGE = "control-dependence";

    private final Graph graph;
    private final Map<Node, Set<Node>> controlDependencies = new HashMap<>();

    /**
     * Constructs a control dependence analysis.
     *
     * @param graph The control flow graph to analyze.
     * @param entryNode The entry node of the graph (used for reachability validation).
     * @param exitNode The exit node of the graph (used as the root for Post-Dominance).
     */
    public ControlDependenceAnalysis(Graph graph, Node entryNode, Node exitNode) {
        this(graph, entryNode, exitNode, new FastDominanceAnalysis(graph, exitNode, true));
    }

    /**
     * Constructs a control dependence analysis using a pre-computed post-dominance analysis.
     *
     * @param graph The control flow graph to analyze.
     * @param entryNode The entry node of the graph (used for reachability validation).
     * @param exitNode The exit node of the graph (used as the root for Post-Dominance).
     * @param postDomAnalysis A pre-computed post-dominance analysis.
     */
    public ControlDependenceAnalysis(Graph graph, Node entryNode, Node exitNode, FastDominanceAnalysis postDomAnalysis) {
        java.util.Objects.requireNonNull(graph, "graph cannot be null");
        java.util.Objects.requireNonNull(entryNode, "Entry node cannot be null");
        java.util.Objects.requireNonNull(exitNode, "Exit node cannot be null");
        java.util.Objects.requireNonNull(postDomAnalysis, "Post-dominance analysis cannot be null");

        this.graph = graph;

        // Validate that the graph is a well-formed CFG (path exists from entry to exit)
        Graph pathGraph = graph.between(entryNode, exitNode);
        if (pathGraph.nodes().isEmpty()) {
            throw new IllegalArgumentException("Graph must contain a path from the given entry to the given exit node");
        }

        Map<Node, Set<Node>> postDomFrontiers = postDomAnalysis.getDominanceFrontiers();

        // 2. Initialize the Control Dependence map for all nodes in the path
        for (Node n : pathGraph.nodes()) {
            controlDependencies.put(n, new HashSet<>());
        }

        // 3. Map Post-Dominance Frontiers to Control Dependencies
        // In FastDominanceAnalysis, PDF(X) = Y means Y is in the post-dominance frontier of X.
        // Therefore, X is control-dependent on Y. (Control flow from Y -> X)
        for (Map.Entry<Node, Set<Node>> entry : postDomFrontiers.entrySet()) {
            Node dependentNode = entry.getKey();   // X

            for (Node controllingNode : entry.getValue()) { // Y
                // Initialize the set if the controlling node wasn't in the path map
                controlDependencies.computeIfAbsent(controllingNode, k -> new HashSet<>()).add(dependentNode);
            }
        }
    }

    /**
     * Returns an unmodifiable map representing the control dependencies.
     * The key is the controlling node (e.g., a branch), and the value is the set of nodes dependent on it.
     * @return the control dependencies map
     */
    public Map<Node, Set<Node>> getControlDependencies() {
        return Collections.unmodifiableMap(controlDependencies);
    }

        /**
     * Injects the computed control dependence edges into the graph.
     * This mutates the underlying graph topology.
     */
    public void injectEdges() {
        for (Map.Entry<Node, Set<Node>> entry : controlDependencies.entrySet()) {
            Node controllingNode = entry.getKey();

            for (Node dependentNode : entry.getValue()) {
                Edge cdEdge = graph.factory().createEdge(controllingNode, dependentNode);

                // Strictly tag the edge rather than setting a boolean attribute
                cdEdge.tags().add(CONTROL_DEPENDENCE_EDGE);

                graph.addEdge(cdEdge);
            }
        }
    }

}
