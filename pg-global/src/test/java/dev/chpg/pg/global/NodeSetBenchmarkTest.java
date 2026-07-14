package dev.chpg.pg.global;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NodeSetBenchmarkTest {

    private GlobalGraph graph;
    private GlobalGraph otherGraph;

    private NodeSet nodes;
    private NodeSet otherNodes;

    @Setup(Level.Trial)
    public void setup() {
        graph = new GlobalGraph();
        GlobalFactory factory = graph.factory();

        for (int i = 0; i < 10_000; i++) {
            Node n = factory.createNode();
            if (i % 2 == 0) n.tags().add("Even");
            if (i % 3 == 0) n.tags().add("Div3");
            if (i % 5 == 0) {
                n.tags().add("Div5");
                n.attributes().put("category", "five");
            }
            graph.addNode(n);
        }
        nodes = graph.nodes();

        otherGraph = new GlobalGraph();
        GlobalFactory otherFactory = otherGraph.factory();
        for (int i = 5_000; i < 15_000; i++) {
            Node n = otherFactory.createNode(); // We just need elements for set ops, IDs might be different but it's fine for testing the structural ops
            // To make intersection meaningful, we'll extract some nodes from the first graph
            if (i % 2 != 0) {
                otherGraph.addNode(n);
            }
        }

        // Populate otherGraph with some overlapping nodes from graph
        int count = 0;
        for (Node n : nodes) {
            if (count % 2 == 0) {
                otherGraph.addNode(n);
            }
            count++;
        }

        otherNodes = otherGraph.nodes();
    }

    @Benchmark
    public void iterateNodes(Blackhole bh) {
        for (Node n : nodes) {
            bh.consume(n);
        }
    }

    @Benchmark
    public void filterWithAttribute(Blackhole bh) {
        NodeSet filtered = nodes.withAttribute("category");
        for (Node n : filtered) {
            bh.consume(n);
        }
    }

    @Benchmark
    public void filterWithAnyTag(Blackhole bh) {
        NodeSet filtered = nodes.withAnyTag("Div3", "Div5");
        for (Node n : filtered) {
            bh.consume(n);
        }
    }

    @Benchmark
    public void toIdArray(Blackhole bh) {
        bh.consume(nodes.toIdArray());
    }

    @Benchmark
    public void union(Blackhole bh) {
        bh.consume(nodes.union(otherNodes));
    }

    @Benchmark
    public void intersection(Blackhole bh) {
        bh.consume(nodes.intersect(otherNodes));
    }

    @Benchmark
    public void difference(Blackhole bh) {
        bh.consume(nodes.difference(otherNodes));
    }

}
