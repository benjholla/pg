package dev.chpg.pg.algorithm;

import dev.chpg.pg.api.Direction;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An O(V + E) implementation of the Cooper, Harvey, and Kennedy algorithm
 * for building the dominator tree and dominance frontiers of a graph.
 *
 * This class strictly performs mathematical analysis. To mutate the underlying
 * graph with the resulting edges, explicitly call injectEdges().
 */
public class FastDominanceAnalysis {

    public static final String DOMINATOR_TREE_EDGE = "idom";
    public static final String POST_DOMINATOR_TREE_EDGE = "ipdom";
    public static final String DOMINANCE_FRONTIER_EDGE = "dom-frontier";
    public static final String POST_DOMINANCE_FRONTIER_EDGE = "pdom-frontier";

    private final Graph graph;
    private final boolean invertEdges;

    private final Map<Node, Node> idomMap = new HashMap<>();
    private final Map<Node, Set<Node>> domTree = new HashMap<>();
    private final Map<Node, Set<Node>> dfMap = new HashMap<>();
    private List<Node> topoTraversal = null;

    /**
     * Constructs a dominance analysis.
     *
     * @param graph The graph to analyze.
     * @param root The root node (Entry node for Dominance, Exit node for Post-Dominance).
     * @param invertEdges True to compute post-dominance.
     */
    public FastDominanceAnalysis(Graph graph, Node root, boolean invertEdges) {
        this.graph = graph;
        this.invertEdges = invertEdges;

        List<Node> rpo = new ArrayList<>();
        Map<Node, Integer> rpoIndex = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        // 1. Compute Reverse Post Order
        postOrderDFS(root, visited, rpo);
        Collections.reverse(rpo);

        int n = rpo.size();
        if (n == 0) return;

        for (int i = 0; i < n; i++) {
            rpoIndex.put(rpo.get(i), i);
            domTree.put(rpo.get(i), new HashSet<>());
            dfMap.put(rpo.get(i), new HashSet<>());
        }

        // 2. CHK Algorithm for Immediate Dominators
        int[] idom = new int[n];
        java.util.Arrays.fill(idom, -1);
        idom[0] = 0;

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < n; i++) {
                Node b = rpo.get(i);
                int newIdom = -1;

                Direction predDir = invertEdges ? Direction.OUT : Direction.IN;
                for (Edge inEdge : graph.edges(b, predDir)) {
                    Node p = invertEdges ? inEdge.to() : inEdge.from();
                    Integer pIndex = rpoIndex.get(p);

                    if (pIndex != null && idom[pIndex] != -1) {
                        if (newIdom == -1) {
                            newIdom = pIndex;
                        } else {
                            int finger1 = pIndex;
                            int finger2 = newIdom;
                            while (finger1 != finger2) {
                                while (finger1 > finger2) finger1 = idom[finger1];
                                while (finger2 > finger1) finger2 = idom[finger2];
                            }
                            newIdom = finger1;
                        }
                    }
                }

                if (newIdom != -1 && idom[i] != newIdom) {
                    idom[i] = newIdom;
                    changed = true;
                }
            }
        }

        // 3. Map Internal Array to Public API & Build Dominator Tree
        for (int i = 1; i < n; i++) {
            if (idom[i] != -1) {
                Node child = rpo.get(i);
                Node parent = rpo.get(idom[i]);
                idomMap.put(child, parent);
                domTree.get(parent).add(child);
            }
        }

        // 4. Compute Dominance Frontiers
        for (int i = 1; i < n; i++) {
            Node b = rpo.get(i);

            List<Node> validPreds = new ArrayList<>();
            Direction predDir = invertEdges ? Direction.OUT : Direction.IN;
            for (Edge inEdge : graph.edges(b, predDir)) {
                Node p = invertEdges ? inEdge.to() : inEdge.from();
                if (rpoIndex.containsKey(p)) {
                    validPreds.add(p);
                }
            }

            if (validPreds.size() >= 2) {
                for (Node p : validPreds) {
                    int runner = rpoIndex.get(p);
                    int targetIdom = idom[i];

                    while (runner != targetIdom && runner != -1) {
                        Node runnerNode = rpo.get(runner);
                        dfMap.get(runnerNode).add(b);
                        runner = idom[runner];
                    }
                }
            }
        }
    }

    private void postOrderDFS(Node n, Set<Node> visited, List<Node> rpo) {
        visited.add(n);
        Direction succDir = invertEdges ? Direction.IN : Direction.OUT;
        for (Edge edge : graph.edges(n, succDir)) {
            Node target = invertEdges ? edge.from() : edge.to();
            if (!visited.contains(target)) {
                postOrderDFS(target, visited, rpo);
            }
        }
        rpo.add(n);
    }

    /**
     * Injects the computed dominator tree and dominance frontier edges into the graph.
     * This mutates the underlying graph topology.
     */
    public void injectEdges() {
        String idomTag = invertEdges ? POST_DOMINATOR_TREE_EDGE : DOMINATOR_TREE_EDGE;
        for (Map.Entry<Node, Node> entry : idomMap.entrySet()) {
            Edge idomEdge = graph.factory().createEdge(entry.getValue(), entry.getKey());
            idomEdge.tags().add(idomTag);
            graph.addEdge(idomEdge);
        }

        String dfTag = invertEdges ? POST_DOMINANCE_FRONTIER_EDGE : DOMINANCE_FRONTIER_EDGE;
        for (Map.Entry<Node, Set<Node>> entry : dfMap.entrySet()) {
            for (Node target : entry.getValue()) {
                Edge dfEdge = graph.factory().createEdge(entry.getKey(), target);
                dfEdge.tags().add(dfTag);
                graph.addEdge(dfEdge);
            }
        }
    }

    public Map<Node, Node> getIdoms() {
        return Collections.unmodifiableMap(idomMap);
    }

    public Map<Node, Set<Node>> getDominatorTree() {
        return Collections.unmodifiableMap(domTree);
    }

    public Map<Node, Set<Node>> getDominanceFrontiers() {
        return Collections.unmodifiableMap(dfMap);
    }

    /**
     * Returns an O(V) topological traversal of the dominator tree.
     */
    public List<Node> topologicalTraversal() {
        if (topoTraversal == null) {
            topoTraversal = new ArrayList<>();
            Set<Node> roots = new HashSet<>(domTree.keySet());
            roots.removeAll(idomMap.keySet()); // Nodes with no dominator parent

            for (Node root : roots) {
                traverseTree(root);
            }
        }
        return Collections.unmodifiableList(topoTraversal);
    }

    private void traverseTree(Node n) {
        topoTraversal.add(n);
        for (Node child : domTree.getOrDefault(n, Collections.emptySet())) {
            traverseTree(child);
        }
    }

    public Iterable<Node> reverseTopologicalTraversal() {
        return () -> {
            List<Node> reversed = new ArrayList<>(topologicalTraversal());
            Collections.reverse(reversed);
            return reversed.iterator();
        };
    }
}
