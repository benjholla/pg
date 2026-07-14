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

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EdgeSetBenchmark {

    @Param({"GLOBAL", "EPHEMERAL", "DEFERRED", "IMMUTABLE"})
    public SetType setType;

    private EdgeSet edges;
    private EdgeSet otherEdges;
    private Edge firstEdge;

    @Setup(Level.Trial)
    public void setup() {
        Graph graph;
        Graph otherGraph;

        if (setType == SetType.GLOBAL || setType == SetType.DEFERRED || setType == SetType.IMMUTABLE) {
            GlobalGraph g = new GlobalGraph();
            GlobalGraph og = new GlobalGraph();
            graph = g;
            otherGraph = og;

            Node n1 = g.factory().createNode();
            Node n2 = g.factory().createNode();
            g.addNode(n1);
            g.addNode(n2);

            for (int i = 0; i < 10_000; i++) {
                Edge e = g.factory().createEdge(n1, n2);
                if (i % 2 == 0) e.tags().add("Even");
                if (i % 3 == 0) e.tags().add("Div3");
                if (i % 5 == 0) {
                    e.tags().add("Div5");
                    e.attributes().put("category", "five");
                }
                e.tags().add("Common");
                g.addEdge(e);
            }

            Node otherN1 = og.factory().createNode();
            Node otherN2 = og.factory().createNode();
            og.addNode(otherN1);
            og.addNode(otherN2);

            for (int i = 5_000; i < 15_000; i++) {
                Edge e = og.factory().createEdge(otherN1, otherN2);
                if (i % 2 != 0) {
                    og.addEdge(e);
                }
            }
        } else {
            EphemeralGraph eg = new EphemeralGraph();
            EphemeralGraph oeg = new EphemeralGraph();
            graph = eg;
            otherGraph = oeg;

            Node n1 = eg.factory().createNode();
            Node n2 = eg.factory().createNode();
            eg.addNode(n1);
            eg.addNode(n2);

            for (int i = 0; i < 10_000; i++) {
                Edge e = eg.factory().createEdge(n1, n2);
                if (i % 2 == 0) e.tags().add("Even");
                if (i % 3 == 0) e.tags().add("Div3");
                if (i % 5 == 0) {
                    e.tags().add("Div5");
                    e.attributes().put("category", "five");
                }
                e.tags().add("Common");
                eg.addEdge(e);
            }

            Node otherN1 = oeg.factory().createNode();
            Node otherN2 = oeg.factory().createNode();
            oeg.addNode(otherN1);
            oeg.addNode(otherN2);

            for (int i = 5_000; i < 15_000; i++) {
                Edge e = oeg.factory().createEdge(otherN1, otherN2);
                if (i % 2 != 0) {
                    oeg.addEdge(e);
                }
            }
        }

        EdgeSet baseEdges = graph.edges();

        int count = 0;
        for (Edge e : baseEdges) {
            if (count % 2 == 0) {
                otherGraph.addEdge(e);
            }
            count++;
        }

        EdgeSet baseOtherEdges = otherGraph.edges();

        for (Edge e : baseEdges) {
            firstEdge = e;
            break;
        }

        switch (setType) {
            case GLOBAL:
            case EPHEMERAL:
                edges = baseEdges;
                otherEdges = baseOtherEdges;
                break;
            case DEFERRED:
                edges = baseEdges.withAnyTag("Even", "Div3", "Div5", "Common");
                otherEdges = baseOtherEdges.withAnyTag("Even", "Div3", "Div5", "Common");
                break;
            case IMMUTABLE:
                edges = baseEdges.materialize();
                otherEdges = baseOtherEdges.materialize();
                break;
        }
    }

    @Benchmark
    public void iterate(Blackhole bh) {
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
    public void withAllTags(Blackhole bh) {
        EdgeSet filtered = edges.withAllTags("Div3", "Div5");
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

    @Benchmark
    public void materialize(Blackhole bh) {
        bh.consume(edges.materialize());
    }

    @Benchmark
    public void size(Blackhole bh) {
        bh.consume(edges.size());
    }

    @Benchmark
    public void contains(Blackhole bh) {
        bh.consume(edges.contains(firstEdge));
    }

    @Benchmark
    public void containsAll(Blackhole bh) {
        bh.consume(edges.containsAll(otherEdges));
    }
}
