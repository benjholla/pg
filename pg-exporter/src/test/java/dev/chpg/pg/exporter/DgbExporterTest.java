package dev.chpg.pg.exporter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DgbExporterTest {

    @Test
    void testExportSimpleGraph() throws Exception {
        // Create a simple export graph
        ExportGraph graph = new ExportGraph() {
            @Override
            public Iterable<? extends ExportNode> nodes() {
                Map<String, ExportAttributeValue> attrs = new HashMap<>();
                attrs.put("name", ExportAttributeValue.ofString("Node1"));
                attrs.put("weight", ExportAttributeValue.ofDouble(1.5));

                return Collections.singletonList(new ExportNode() {
                    @Override
                    public int id() { return 1; }

                    @Override
                    public Iterable<String> tags() { return Arrays.asList("tag1", "tag2"); }

                    @Override
                    public Map<String, ExportAttributeValue> attributes() { return attrs; }
                });
            }

            @Override
            public Iterable<? extends ExportEdge> edges() {
                Map<String, ExportAttributeValue> attrs = new HashMap<>();
                attrs.put("distance", ExportAttributeValue.ofInt(10));

                return Collections.singletonList(new ExportEdge() {
                    @Override
                    public int id() { return 100; }

                    @Override
                    public int sourceId() { return 1; }

                    @Override
                    public int targetId() { return 1; }

                    @Override
                    public Iterable<String> tags() { return Collections.singletonList("loop"); }

                    @Override
                    public Map<String, ExportAttributeValue> attributes() { return attrs; }
                });
            }
        };

        File tempFile = Files.createTempFile("test-graph", ".dgb").toFile();
        tempFile.deleteOnExit();

        DgbExporter exporter = new DgbExporter();
        exporter.export(graph, tempFile.getAbsolutePath());

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0, "The exported file should not be empty.");
    }
}
