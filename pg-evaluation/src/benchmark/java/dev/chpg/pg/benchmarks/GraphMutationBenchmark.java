package dev.chpg.pg.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.io.DirectGraphBufferReader;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
public class GraphMutationBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        public GlobalGraph globalGraph;
        public Universe universe;

        // Array index = watermark ID, Array value = Graph Engine Node ID
        public int[] globalFunctionIds;
        public int[] universeFunctionIds;

        @Param({"xinu_watermarked.dgb"})
        public String dgbFilePath;

        @Setup(Level.Trial)
        public void setupBaseGraphs() throws IOException {
            Path inputPath = Path.of(dgbFilePath);
            System.out.println("Hydrating base graphs from: " + inputPath);

            this.globalGraph = new GlobalGraph();
            try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
                DirectGraphBufferReader.read(channel, this.globalGraph, this.globalGraph.factory(), this.globalGraph.factory());
            }

            this.universe = new Universe();
            try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
                EphemeralGraph setupTransaction = new EphemeralGraph(this.universe);
                DirectGraphBufferReader.read(channel, setupTransaction, setupTransaction.factory(), setupTransaction.factory());
                this.universe.promote(setupTransaction);
            }

            // 2. Discover the highest watermark to size the arrays
            int maxWatermark = -1;
            for (Node n : globalGraph.nodes()) {
                if (n.attributes().containsKey("benchmark.watermark")) {
                    int wm = ((AttributeValue.IntegerValue) n.attributes().get("benchmark.watermark")).value();
                    maxWatermark = Math.max(maxWatermark, wm);
                }
            }

            if (maxWatermark == -1) {
                System.err.println("WARNING: No nodes found with 'benchmark.watermark' attribute.");
                globalFunctionIds = new int[0];
                universeFunctionIds = new int[0];
                return;
            }

            globalFunctionIds = new int[maxWatermark + 1];
            universeFunctionIds = new int[maxWatermark + 1];

            // 3. Map the watermarks to the internal engine IDs
            for (Node n : globalGraph.nodes()) {
                if (n.attributes().containsKey("benchmark.watermark")) {
                    int wm = ((AttributeValue.IntegerValue) n.attributes().get("benchmark.watermark")).value();
                    globalFunctionIds[wm] = n.id();
                }
            }

            Graph ug = this.universe.asGraph();
            for (Node n : ug.nodes()) {
                if (n.attributes().containsKey("benchmark.watermark")) {
                    int wm = ((AttributeValue.IntegerValue) n.attributes().get("benchmark.watermark")).value();
                    universeFunctionIds[wm] = n.id();
                }
            }
        }
    }

    /**
     * Adapt your existing dominance logic to accept the standard Graph API.
     * It should traverse the graph starting from the entry node, compute dominators,
     * and inject the new Control Dependence edges.
     */
    private void runDominanceAnalysis(Graph graph, Node entryNode) {
        // TODO: Compute Dominator Tree
        // TODO: Compute Dominance Frontiers
        // TODO: Inject Control Dependence edges: graph.addEdge(...)
    }

    @Benchmark
    public void measureGlobalMutation(BenchmarkState state) {
        if (state.globalFunctionIds.length == 0) { return; }

        int entryNodeId = state.globalFunctionIds[0];
        Node targetFunctionEntry = state.globalGraph.node(entryNodeId).orElseThrow();

        // WARNING: If runDominanceAnalysis modifies globalGraph directly,
        // the graph will grow every iteration. You may need to have the algorithm
        // return a list of pending edges instead, or manually remove them at the end of the method.
        runDominanceAnalysis(state.globalGraph, targetFunctionEntry);
    }

    @Benchmark
    public void measureMultiverseMutation(BenchmarkState state) {
        if (state.universeFunctionIds.length == 0) { return; }

        // 1. Open the $O(1)$ Transaction
        EphemeralGraph transaction = new EphemeralGraph(state.universe);

        // 2. Run the analysis completely isolated in the sandbox
        // The algorithm queries the baseline Universe but writes only to the Ephemeral delta
        int entryNodeId = state.universeFunctionIds[0];
        Node entryInSandbox = transaction.node(entryNodeId).orElseThrow();
        runDominanceAnalysis(transaction, entryInSandbox);

        // 3. Promote the patch to the core engine
        // (Note: This WILL permanently grow the Universe over the JMH run.
        // If you want to measure pure compute without growth, omit this line and let the transaction GC).
        state.universe.promote(transaction);
    }
}
