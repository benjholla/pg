package dev.chpg.pg.evaluation;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NodeSetBenchmark {

    @Param({"GLOBAL", "EPHEMERAL", "DEFERRED", "IMMUTABLE"})
    public SetType setType;

    private NodeSet nodes;
    private NodeSet otherNodes;
    private Node firstNode;

    @Setup(Level.Trial)
    public void setup() {
        Graph graph;
        Graph otherGraph;

        if (setType == SetType.GLOBAL || setType == SetType.DEFERRED || setType == SetType.IMMUTABLE) {
            GlobalGraph g = new GlobalGraph();
            GlobalGraph og = new GlobalGraph();
            graph = g;
            otherGraph = og;

            for (int i = 0; i < 10_000; i++) {
                Node n = g.factory().createNode();
                if (i % 2 == 0) { n.tags().add("Even"); }
                if (i % 3 == 0) { n.tags().add("Div3"); }
                if (i % 5 == 0) {
                    n.tags().add("Div5");
                    n.attributes().put("category", "five");
                }
                n.tags().add("Common");
                g.addNode(n);
            }
            for (int i = 5_000; i < 15_000; i++) {
                Node n = og.factory().createNode();
                if (i % 2 != 0) {
                    og.addNode(n);
                }
            }
        } else {
            EphemeralGraph eg = new EphemeralGraph();
            EphemeralGraph oeg = new EphemeralGraph();
            graph = eg;
            otherGraph = oeg;

            for (int i = 0; i < 10_000; i++) {
                Node n = eg.factory().createNode();
                if (i % 2 == 0) { n.tags().add("Even"); }
                if (i % 3 == 0) { n.tags().add("Div3"); }
                if (i % 5 == 0) {
                    n.tags().add("Div5");
                    n.attributes().put("category", "five");
                }
                n.tags().add("Common");
                eg.addNode(n);
            }
            for (int i = 5_000; i < 15_000; i++) {
                Node n = oeg.factory().createNode();
                if (i % 2 != 0) {
                    oeg.addNode(n);
                }
            }
        }

        NodeSet baseNodes = graph.nodes();

        int count = 0;
        for (Node n : baseNodes) {
            if (count % 2 == 0) {
                otherGraph.addNode(n);
            }
            count++;
        }

        NodeSet baseOtherNodes = otherGraph.nodes();

        for (Node n : baseNodes) {
            firstNode = n;
            break;
        }

        switch (setType) {
            case GLOBAL:
            case EPHEMERAL:
                nodes = baseNodes;
                otherNodes = baseOtherNodes;
                break;
            case DEFERRED:
                nodes = baseNodes.withAnyTag("Even", "Div3", "Div5", "Common");
                otherNodes = baseOtherNodes.withAnyTag("Even", "Div3", "Div5", "Common");
                break;
            case IMMUTABLE:
                nodes = baseNodes.materialize();
                otherNodes = baseOtherNodes.materialize();
                break;
        }
    }

    @Benchmark
    public void iterate(Blackhole bh) {
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
    public void withAllTags(Blackhole bh) {
        NodeSet filtered = nodes.withAllTags("Div3", "Div5");
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

    @Benchmark
    public void materialize(Blackhole bh) {
        bh.consume(nodes.materialize());
    }

    @Benchmark
    public void size(Blackhole bh) {
        bh.consume(nodes.size());
    }

    @Benchmark
    public void contains(Blackhole bh) {
        bh.consume(nodes.contains(firstNode));
    }

    @Benchmark
    public void containsAll(Blackhole bh) {
        bh.consume(nodes.containsAll(otherNodes));
    }
}
