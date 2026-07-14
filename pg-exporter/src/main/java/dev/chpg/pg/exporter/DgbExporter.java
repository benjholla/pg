package dev.chpg.pg.exporter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DgbExporter {

    public void export(ExportGraph graph, String outputPath) throws Exception {
        Map<String, Integer> dictionary = new HashMap<>();
        int[] nextId = {0}; // Array used to allow mutation inside lambdas/helpers

        // ==========================================
        // PASS 1: COMPUTE DICTIONARY
        // ==========================================
        for (ExportNode node : graph.nodes()) {
            harvestStrings(node.tags(), node.attributes(), dictionary, nextId);
        }
        for (ExportEdge edge : graph.edges()) {
            harvestStrings(edge.tags(), edge.attributes(), dictionary, nextId);
        }

        // ==========================================
        // PASS 2: BINARY STREAMING
        // ==========================================
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath), 1024 * 1024))) { // 1MB Buffer

            // 1. Header
            out.writeUTF("DGB");
            out.writeInt(1); // Version

            // 2. Write Dictionary
            out.writeInt(dictionary.size());
            String[] reverseDict = new String[dictionary.size()];
            for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
                reverseDict[entry.getValue()] = entry.getKey();
            }
            for (String str : reverseDict) {
                byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            }

            // 3. Write Nodes
            // (Note: To write the count, the adapter either needs to provide it,
            // or we count during Pass 1. Assuming a standard Iterable for now).
            int nodeCount = countIterable(graph.nodes());
            out.writeInt(nodeCount);

            for (ExportNode node : graph.nodes()) {
                out.writeInt(node.id());
                writeTags(node.tags(), dictionary, out);
                writeAttributes(node.attributes(), dictionary, out);
            }

            // 4. Write Edges
            int edgeCount = countIterable(graph.edges());
            out.writeInt(edgeCount);

            for (ExportEdge edge : graph.edges()) {
                out.writeInt(edge.id());
                out.writeInt(edge.sourceId());
                out.writeInt(edge.targetId());
                writeTags(edge.tags(), dictionary, out);
                writeAttributes(edge.attributes(), dictionary, out);
            }
        }
    }

    // --- Helper Methods to keep the code clean ---

    private void harvestStrings(Iterable<String> tags, Map<String, ExportAttributeValue> attributes,
                                Map<String, Integer> dict, int[] nextId) {
        if (tags != null) {
            for (String tag : tags) {
                if (!dict.containsKey(tag)) { dict.put(tag, nextId[0]++); }
            }
        }
        if (attributes != null) {
            for (Map.Entry<String, ExportAttributeValue> entry : attributes.entrySet()) {
                String key = entry.getKey();
                if (!dict.containsKey(key)) { dict.put(key, nextId[0]++); }

                // Only String values go into the dictionary pool
                ExportAttributeValue val = entry.getValue();
                if (val != null && val.getType() == ExportAttributeValue.Type.STRING) {
                    String strVal = (String) val.getValue();
                    if (!dict.containsKey(strVal)) { dict.put(strVal, nextId[0]++); }
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
                    out.writeBoolean((Boolean) val.getValue());
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
        if (it instanceof java.util.Collection) return ((java.util.Collection<?>) it).size();
        int count = 0;
        for (Object o : it) count++;
        return count;
    }
}
