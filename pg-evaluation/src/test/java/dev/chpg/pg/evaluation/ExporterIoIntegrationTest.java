package dev.chpg.pg.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.exporter.DgbExporter;
import dev.chpg.pg.exporter.ExportAttributeValue;
import dev.chpg.pg.exporter.ExportEdge;
import dev.chpg.pg.exporter.ExportGraph;
import dev.chpg.pg.exporter.ExportNode;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.io.DirectGraphBufferReader;

public class ExporterIoIntegrationTest {

    @Test
    public void testExporterAndIoIntegration() throws Exception {
        GlobalGraph originalGraph = new GlobalGraph();

        Node n1 = originalGraph.factory().createNode();
        n1.tags().add("User");
        n1.attributes().put("watermark", AttributeValue.value("node_1"));
        n1.attributes().put("age", AttributeValue.value(30));
        originalGraph.addNode(n1);

        Node n2 = originalGraph.factory().createNode();
        n2.tags().add("User");
        n2.attributes().put("watermark", AttributeValue.value("node_2"));
        n2.attributes().put("active", AttributeValue.value(true));
        originalGraph.addNode(n2);

        Edge e1 = originalGraph.factory().createEdge(n1, n2);
        e1.tags().add("Knows");
        e1.attributes().put("since", AttributeValue.value(2020));
        originalGraph.addEdge(e1);

        File tempFile = Files.createTempFile("pg-integration", ".dgb").toFile();
        tempFile.deleteOnExit();

        DgbExporter exporter = new DgbExporter();
        exporter.export(new ExportGraphAdapter(originalGraph), tempFile.getAbsolutePath());

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);

        GlobalGraph importedGraph = new GlobalGraph();
        try (FileInputStream fis = new FileInputStream(tempFile);
             FileChannel channel = fis.getChannel()) {
            DirectGraphBufferReader.read(channel, importedGraph, importedGraph.factory(), importedGraph.factory());
        }

        assertEquals(originalGraph.nodes().size(), importedGraph.nodes().size());
        assertEquals(originalGraph.edges().size(), importedGraph.edges().size());

        Node importedN1 = findByWatermark(importedGraph, "node_1");
        Node importedN2 = findByWatermark(importedGraph, "node_2");

        assertNotNull(importedN1);
        assertNotNull(importedN2);

        assertTrue(importedN1.tags().contains("User"));
        assertEquals(30, ((AttributeValue.IntegerValue) importedN1.attributes().get("age")).value());

        assertTrue(importedN2.tags().contains("User"));
        assertTrue(((AttributeValue.BooleanValue) importedN2.attributes().get("active")).value());

        boolean foundEdge = false;
        for (Edge edge : importedGraph.edges()) {
            if (edge.from().id() == importedN1.id() && edge.to().id() == importedN2.id()) {
                foundEdge = true;
                assertTrue(edge.tags().contains("Knows"));
                assertEquals(2020, ((AttributeValue.IntegerValue) edge.attributes().get("since")).value());
            }
        }
        assertTrue(foundEdge, "The edge between n1 and n2 was not found or did not match properties.");
    }

    private Node findByWatermark(Graph graph, String watermark) {
        for (Node node : graph.nodes()) {
            AttributeValue val = node.attributes().get("watermark");
            if (val instanceof AttributeValue.StringValue) {
                if (watermark.equals(((AttributeValue.StringValue) val).value())) {
                    return node;
                }
            }
        }
        return null;
    }

    // Adapters for pg-exporter

    private static class ExportGraphAdapter implements ExportGraph {
        private final Graph graph;

        ExportGraphAdapter(Graph graph) {
            this.graph = graph;
        }

        @Override
        public Iterable<? extends ExportNode> nodes() {
            return StreamSupport.stream(graph.nodes().spliterator(), false)
                    .map(ExportNodeAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public Iterable<? extends ExportEdge> edges() {
            return StreamSupport.stream(graph.edges().spliterator(), false)
                    .map(ExportEdgeAdapter::new)
                    .collect(Collectors.toList());
        }
    }

    private static class ExportNodeAdapter implements ExportNode {
        private final Node node;

        ExportNodeAdapter(Node node) {
            this.node = node;
        }

        @Override
        public int id() {
            return node.id();
        }

        @Override
        public Iterable<String> tags() {
            return node.tags();
        }

        @Override
        public Map<String, ExportAttributeValue> attributes() {
            return convertAttributes(node.attributes());
        }
    }

    private static class ExportEdgeAdapter implements ExportEdge {
        private final Edge edge;

        ExportEdgeAdapter(Edge edge) {
            this.edge = edge;
        }

        @Override
        public int id() {
            return edge.id();
        }

        @Override
        public int sourceId() {
            return edge.from().id();
        }

        @Override
        public int targetId() {
            return edge.to().id();
        }

        @Override
        public Iterable<String> tags() {
            return edge.tags();
        }

        @Override
        public Map<String, ExportAttributeValue> attributes() {
            return convertAttributes(edge.attributes());
        }
    }

    private static Map<String, ExportAttributeValue> convertAttributes(Map<String, AttributeValue> attrs) {
        Map<String, ExportAttributeValue> expAttrs = new HashMap<>();
        for (Map.Entry<String, AttributeValue> entry : attrs.entrySet()) {
            AttributeValue val = entry.getValue();
            if (val instanceof AttributeValue.StringValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofString(((AttributeValue.StringValue) val).value()));
            } else if (val instanceof AttributeValue.IntegerValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofInt(((AttributeValue.IntegerValue) val).value()));
            } else if (val instanceof AttributeValue.LongValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofLong(((AttributeValue.LongValue) val).value()));
            } else if (val instanceof AttributeValue.DoubleValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofDouble(((AttributeValue.DoubleValue) val).value()));
            } else if (val instanceof AttributeValue.BooleanValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofBoolean(((AttributeValue.BooleanValue) val).value()));
            } else if (val instanceof AttributeValue.ByteArrayValue) {
                expAttrs.put(entry.getKey(), ExportAttributeValue.ofByteArray(((AttributeValue.ByteArrayValue) val).value()));
            }
        }
        return expAttrs;
    }
}
