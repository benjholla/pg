package dev.chpg.pg.evaluation;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.algorithm.ControlDependenceAnalysis;
import dev.chpg.pg.algorithm.FastDominanceAnalysis;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DominanceAnalysisBenchmark {

    @Param({"GLOBAL", "EPHEMERAL"})
    public String setType;

    private Graph graph;
    private Node root;
    private Node exitNode;

    @Setup(Level.Trial)
    public void setup() {
        if ("GLOBAL".equals(setType)) {
            GlobalGraph g = new GlobalGraph();
            graph = g;
            root = g.factory().createNode();
            g.addNode(root);
            buildRandomGraph(g, root, 1000, 2000);
        } else {
            EphemeralGraph eg = new EphemeralGraph(new Universe());
            graph = eg;
            root = eg.factory().createNode();
            eg.addNode(root);
            buildRandomGraph(eg, root, 1000, 2000);
        }
    }

    private void buildRandomGraph(Graph g, Node rootNode, int numNodes, int numEdges) {
        Node[] nodes = new Node[numNodes];
        nodes[0] = rootNode;

        for (int i = 1; i < numNodes; i++) {
            nodes[i] = g.factory().createNode();
            g.addNode(nodes[i]);
            // Ensure graph is somewhat connected
            g.addEdge(g.factory().createEdge(nodes[i / 2], nodes[i]));
        }

        Random rand = new Random(42);
        for (int i = 0; i < numEdges - numNodes + 1; i++) {
            Node src = nodes[rand.nextInt(numNodes)];
            Node dst = nodes[rand.nextInt(numNodes)];
            g.addEdge(g.factory().createEdge(src, dst));
        }

        exitNode = nodes[numNodes - 1];
    }

    @State(Scope.Thread)
    public static class AnalysisState {
        public FastDominanceAnalysis analysis;

        @Setup(Level.Invocation)
        public void setupAnalysis(DominanceAnalysisBenchmark benchmark) {
            analysis = new FastDominanceAnalysis(benchmark.graph, benchmark.root, false);
        }
    }

    // Benchmark 1: Pure analytical speed
    @Benchmark
    public FastDominanceAnalysis benchmarkAnalysis() {
        return new FastDominanceAnalysis(graph, root, false);
    }

    // Benchmark 2: Transactional mutation speed
    @Benchmark
    public void benchmarkMutation(AnalysisState state) {
        state.analysis.injectEdges(); // Measures Graph overlay speed
    }

    @State(Scope.Thread)
    public static class CDState {
        public ControlDependenceAnalysis analysis;
        public FastDominanceAnalysis postDomAnalysis;

        @Setup(Level.Invocation)
        public void setupCD(DominanceAnalysisBenchmark benchmark) {
            postDomAnalysis = new FastDominanceAnalysis(benchmark.graph, benchmark.exitNode, true);
            analysis = new ControlDependenceAnalysis(benchmark.graph, benchmark.root, benchmark.exitNode, postDomAnalysis);
        }
    }

    // Benchmark 3: Pure control dependence speed
    @Benchmark
    public ControlDependenceAnalysis benchmarkCDAnalysis(CDState state) {
        return new ControlDependenceAnalysis(graph, root, exitNode, state.postDomAnalysis);
    }

    // Benchmark 4: CD mutation speed
    @Benchmark
    public void benchmarkCDMutation(CDState state) {
        state.analysis.injectEdges(); // Measures Graph overlay speed
    }
}
