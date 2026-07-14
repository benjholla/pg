package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EdgeSetBenchmarkTest {

    private GlobalGraph graph;
    private GlobalGraph otherGraph;

    private EdgeSet edges;
    private EdgeSet otherEdges;

    @Setup(Level.Trial)
    public void setup() {
        graph = new GlobalGraph();
        GlobalFactory factory = graph.factory();

        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        graph.addNode(n1);
        graph.addNode(n2);

        for (int i = 0; i < 10_000; i++) {
            Edge e = factory.createEdge(n1, n2);
            if (i % 2 == 0) e.tags().add("Even");
            if (i % 3 == 0) e.tags().add("Div3");
            if (i % 5 == 0) {
                e.tags().add("Div5");
                e.attributes().put("category", "five");
            }
            graph.addEdge(e);
        }
        edges = graph.edges();

        otherGraph = new GlobalGraph();
        GlobalFactory otherFactory = otherGraph.factory();

        Node otherN1 = otherFactory.createNode();
        Node otherN2 = otherFactory.createNode();
        otherGraph.addNode(otherN1);
        otherGraph.addNode(otherN2);

        for (int i = 5_000; i < 15_000; i++) {
            Edge e = otherFactory.createEdge(otherN1, otherN2);
            if (i % 2 != 0) {
                otherGraph.addEdge(e);
            }
        }

        int count = 0;
        for (Edge e : edges) {
            if (count % 2 == 0) {
                otherGraph.addEdge(e);
            }
            count++;
        }

        otherEdges = otherGraph.edges();
    }

    @Benchmark
    public void iterateEdges(Blackhole bh) {
        for (Edge e : edges) {
            bh.consume(e);
        }
    }

    @Benchmark
    public void filterWithAttribute(Blackhole bh) {
        EdgeSet filtered = edges.withAttribute("category");
        for (Edge e : filtered) {
            bh.consume(e);
        }
    }

    @Benchmark
    public void filterWithAnyTag(Blackhole bh) {
        EdgeSet filtered = edges.withAnyTag("Div3", "Div5");
        for (Edge e : filtered) {
            bh.consume(e);
        }
    }

    @Benchmark
    public void toIdArray(Blackhole bh) {
        bh.consume(edges.toIdArray());
    }

    @Benchmark
    public void union(Blackhole bh) {
        bh.consume(edges.union(otherEdges));
    }

    @Benchmark
    public void intersection(Blackhole bh) {
        bh.consume(edges.intersect(otherEdges));
    }

    @Benchmark
    public void difference(Blackhole bh) {
        bh.consume(edges.difference(otherEdges));
    }

    @Test
    public void runBenchmarks() throws Exception {
        if (this.getClass() != EdgeSetBenchmarkTest.class) {
            return;
        }

        Options opt = new OptionsBuilder()
                .include(EdgeSetBenchmarkTest.class.getName())
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .forks(1)
                .shouldFailOnError(true)
                .build();

        new Runner(opt).run();
    }
}
