package dev.chpg.pg.exporter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class DgbExporter {

    public void export(ExportGraph graph, String outputPath) throws Exception {
        Map<String, Integer> dictionary = new LinkedHashMap<>();

        // ==========================================
        // PASS 1: COMPUTE DICTIONARY
        // ==========================================
        for (ExportNode node : graph.nodes()) {
            harvestStrings(node.tags(), node.attributes(), dictionary);
        }
        for (ExportEdge edge : graph.edges()) {
            harvestStrings(edge.tags(), edge.attributes(), dictionary);
        }

        // ==========================================
        // PASS 2: BINARY STREAMING
        // ==========================================
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath), 1024 * 1024))) { // 1MB Buffer

            // 1. Header
            out.writeInt(0x44474201); // MAGIC_HEADER
            int nodeCount = countIterable(graph.nodes());
            int edgeCount = countIterable(graph.edges());
            out.writeInt(nodeCount);
            out.writeInt(edgeCount);

            // 2. Write Dictionary
            out.writeInt(dictionary.size());
            for (String str : dictionary.keySet()) {
                byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            }

            // 3. Write Nodes
            for (ExportNode node : graph.nodes()) {
                out.writeInt(node.id());
                writeTags(node.tags(), dictionary, out);
                writeAttributes(node.attributes(), dictionary, out);
            }

            // 4. Write Edges
            for (ExportEdge edge : graph.edges()) {
                out.writeInt(edge.id());
                out.writeInt(edge.sourceId());
                out.writeInt(edge.targetId());
                writeTags(edge.tags(), dictionary, out);
                writeAttributes(edge.attributes(), dictionary, out);
            }

            // 5. Write Footer
            out.write(new byte[]{0x45, 0x4F, 0x46, 0x44, 0x47, 0x42});
        }
    }

    // --- Helper Methods to keep the code clean ---

    private void harvestStrings(Iterable<String> tags, Map<String, ExportAttributeValue> attributes,
                                Map<String, Integer> dict) {
        if (tags != null) {
            for (String tag : tags) {
                dict.putIfAbsent(tag, dict.size());
            }
        }
        if (attributes != null) {
            for (Map.Entry<String, ExportAttributeValue> entry : attributes.entrySet()) {
                String key = entry.getKey();
                dict.putIfAbsent(key, dict.size());

                // Only String values go into the dictionary pool
                ExportAttributeValue val = entry.getValue();
                if (val != null && val.getType() == ExportAttributeValue.Type.STRING) {
                    String strVal = (String) val.getValue();
                    dict.putIfAbsent(strVal, dict.size());
                }
            }
        }
    }

    private void writeTags(Iterable<String> tags, Map<String, Integer> dict, DataOutputStream out) throws Exception {
        if (tags == null) {
            out.writeInt(0);
            return;
        }
        int count = countIterable(tags);
        out.writeInt(count);
        for (String tag : tags) {
            out.writeInt(dict.get(tag));
        }
    }

    private void writeAttributes(Map<String, ExportAttributeValue> attributes, Map<String, Integer> dict, DataOutputStream out) throws Exception {
        if (attributes == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(attributes.size());
        for (Map.Entry<String, ExportAttributeValue> entry : attributes.entrySet()) {
            out.writeInt(dict.get(entry.getKey())); // Write Key ID

            ExportAttributeValue val = entry.getValue();
            out.writeByte(val.getType().marker);    // Write Type Marker

            // Write Value Payload
            switch (val.getType()) {
                case STRING:
                    out.writeInt(dict.get((String) val.getValue()));
                    break;
                case INTEGER:
                    out.writeInt((Integer) val.getValue());
                    break;
                case LONG:
                    out.writeLong((Long) val.getValue());
                    break;
                case BOOLEAN:
                    out.writeByte(((Boolean) val.getValue()) ? 1 : 0);
                    break;
                case DOUBLE:
                    out.writeDouble((Double) val.getValue());
                    break;
                case BYTE_ARRAY:
                    byte[] bytes = (byte[]) val.getValue();
                    out.writeInt(bytes.length);
                    out.write(bytes);
                    break;
            }
        }
    }

    private int countIterable(Iterable<?> it) {
        if (it == null) return 0;
        // This handles both the Collection optimization and the fallback loop
        // automatically
        // When you call it.spliterator(), Java is smart enough to check if the Iterable
        // is actually a Collection. If it is, the resulting stream is automatically
        // sized, and .count() will return the size in O(1) time without actually
        // iterating.
        return (int) StreamSupport.stream(it.spliterator(), false).count();
    }
}
