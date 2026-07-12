package io.github.benjholla.pg.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeFactory;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.GraphFactory;
import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeFactory;
import io.github.benjholla.pg.global.GlobalGraph;

public class DirectGraphBufferTest {

    @Test
    public void testRoundTripSerialization(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.dgb");

        // 1. Create source graph
        GlobalGraph sourceGraph = new GlobalGraph();
        NodeFactory nf = sourceGraph.factory();
        EdgeFactory ef = sourceGraph.factory();

        Node n1 = nf.createNode();
        Node n2 = nf.createNode();
        Node n3 = nf.createNode();

        sourceGraph.addNode(n1);
        sourceGraph.addNode(n2);
        sourceGraph.addNode(n3);

        Edge e1 = ef.createEdge(n1, n2);
        Edge e2 = ef.createEdge(n2, n3);

        sourceGraph.addEdge(e1);
        sourceGraph.addEdge(e2);

        // 2. Write graph
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            DirectGraphBufferWriter.write(sourceGraph, channel, 1024); // Use small buffer to test chunking
        }

        // 3. Read graph
        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, targetGraph, nf, ef, 1024);
        }

        // 4. Verify
        assertEquals(3, targetGraph.nodes().size(), "Should have 3 nodes");
        assertEquals(2, targetGraph.edges().size(), "Should have 2 edges");
    }

    @Test
    public void testMissingFooter(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bad.dgb");

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            buffer.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            buffer.putInt(0); // 0 nodes
            buffer.putShort((short)0); // incomplete
            buffer.flip();
            channel.write(buffer);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            assertThrows(CorruptedGraphBufferException.class, () -> {
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            }, "Expected CorruptedGraphBufferException due to missing/invalid footer.");
        }
    }

    @Test
    public void testEmptyGraphSerialization(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.dgb");

        GlobalGraph sourceGraph = new GlobalGraph();

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            DirectGraphBufferWriter.write(sourceGraph, channel, 1024);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory(), 1024);
        }

        assertEquals(0, targetGraph.nodes().size(), "Should have 0 nodes");
        assertEquals(0, targetGraph.edges().size(), "Should have 0 edges");
    }

    @Test
    public void testAllAttributeTypesSerialization(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("attributes.dgb");

        GlobalGraph sourceGraph = new GlobalGraph();
        NodeFactory nf = sourceGraph.factory();

        Node n = nf.createNode();
        n.attributes().put("attr_string", AttributeValue.value("test_string"));
        n.attributes().put("attr_boolean", AttributeValue.value(true));
        n.attributes().put("attr_int", AttributeValue.value(42));
        n.attributes().put("attr_long", AttributeValue.value(9999999999L));
        n.attributes().put("attr_double", AttributeValue.value(3.14159));
        n.attributes().put("attr_byte_array", AttributeValue.value(new byte[]{1, 2, 3, 4, 5}));

        sourceGraph.addNode(n);

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            DirectGraphBufferWriter.write(sourceGraph, channel, 1024);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory(), 1024);
        }

        assertEquals(1, targetGraph.nodes().size(), "Should have 1 node");
        Node parsedNode = targetGraph.nodes().iterator().next();
        assertEquals("test_string", ((AttributeValue.StringValue) parsedNode.attributes().get("attr_string")).value());
        assertTrue(((AttributeValue.BooleanValue) parsedNode.attributes().get("attr_boolean")).value());
        assertEquals(42, ((AttributeValue.IntegerValue) parsedNode.attributes().get("attr_int")).value());
        assertEquals(9999999999L, ((AttributeValue.LongValue) parsedNode.attributes().get("attr_long")).value());
        assertEquals(3.14159, ((AttributeValue.DoubleValue) parsedNode.attributes().get("attr_double")).value());

        byte[] arr = ((AttributeValue.ByteArrayValue) parsedNode.attributes().get("attr_byte_array")).value();
        assertEquals(5, arr.length);
        assertEquals(1, arr[0]);
        assertEquals(5, arr[4]);
    }

    @Test
    public void testDictionaryDeduplication(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("dict.dgb");

        GlobalGraph sourceGraph = new GlobalGraph();
        NodeFactory nf = sourceGraph.factory();
        EdgeFactory ef = sourceGraph.factory();

        Node n1 = nf.createNode();
        n1.tags().add("shared_tag");
        n1.attributes().put("shared_key", AttributeValue.value("shared_value"));

        Node n2 = nf.createNode();
        n2.tags().add("shared_tag");
        n2.attributes().put("shared_key", AttributeValue.value("shared_value"));

        Edge e1 = ef.createEdge(n1, n2);
        e1.tags().add("shared_tag");
        e1.attributes().put("shared_key", AttributeValue.value("shared_value"));

        sourceGraph.addNode(n1);
        sourceGraph.addNode(n2);
        sourceGraph.addEdge(e1);

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            DirectGraphBufferWriter.write(sourceGraph, channel, 1024);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory(), 1024);
        }

        assertEquals(2, targetGraph.nodes().size());
        assertEquals(1, targetGraph.edges().size());

        for (Node n : targetGraph.nodes()) {
            assertTrue(n.tags().contains("shared_tag"));
            assertEquals("shared_value", ((AttributeValue.StringValue) n.attributes().get("shared_key")).value());
        }

        Edge e = targetGraph.edges().iterator().next();
        assertTrue(e.tags().contains("shared_tag"));
        assertEquals("shared_value", ((AttributeValue.StringValue) e.attributes().get("shared_key")).value());
    }

    @Test
    public void testByteArrayThreeTierStrategy(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bytearray.dgb");

        GlobalGraph sourceGraph = new GlobalGraph();
        NodeFactory nf = sourceGraph.factory();

        int bufferSize = 1024;

        // Tier 1: Fits in current chunk space
        byte[] tier1 = new byte[100];
        for (int i = 0; i < tier1.length; i++) tier1[i] = (byte) i;

        // Tier 2: Doesn't fit in remaining space but fits in total capacity
        // To trigger this, we need to partially fill the buffer first
        byte[] tier2Padding = new byte[800];
        byte[] tier2 = new byte[500]; // 500 > (1024 - 800) but 500 < 1024
        for (int i = 0; i < tier2.length; i++) tier2[i] = (byte) (i % 256);

        // Tier 3: Exceeds total capacity
        byte[] tier3 = new byte[2000];
        for (int i = 0; i < tier3.length; i++) tier3[i] = (byte) (i % 256);

        Node n1 = nf.createNode();
        n1.attributes().put("tier1", AttributeValue.value(tier1));

        Node n2 = nf.createNode();
        n2.attributes().put("padding", AttributeValue.value(tier2Padding));
        n2.attributes().put("tier2", AttributeValue.value(tier2));

        Node n3 = nf.createNode();
        n3.attributes().put("tier3", AttributeValue.value(tier3));

        sourceGraph.addNode(n1);
        sourceGraph.addNode(n2);
        sourceGraph.addNode(n3);

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            DirectGraphBufferWriter.write(sourceGraph, channel, bufferSize);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory(), bufferSize);
        }

        assertEquals(3, targetGraph.nodes().size());

        boolean foundTier1 = false;
        boolean foundTier2 = false;
        boolean foundTier3 = false;

        for (Node n : targetGraph.nodes()) {
            if (n.attributes().containsKey("tier1")) {
                byte[] val = ((AttributeValue.ByteArrayValue) n.attributes().get("tier1")).value();
                assertEquals(100, val.length);
                assertEquals(50, val[50]);
                foundTier1 = true;
            }
            if (n.attributes().containsKey("tier2")) {
                byte[] val = ((AttributeValue.ByteArrayValue) n.attributes().get("tier2")).value();
                assertEquals(500, val.length);
                assertEquals((byte) (300 % 256), val[300]);
                foundTier2 = true;
            }
            if (n.attributes().containsKey("tier3")) {
                byte[] val = ((AttributeValue.ByteArrayValue) n.attributes().get("tier3")).value();
                assertEquals(2000, val.length);
                assertEquals((byte) (1500 % 256), val[1500]);
                foundTier3 = true;
            }
        }

        assertTrue(foundTier1);
        assertTrue(foundTier2);
        assertTrue(foundTier3);
    }

    @Test
    public void testFileTooSmall(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("small.dgb");

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(5);
            buffer.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            buffer.put((byte)0);
            buffer.flip();
            channel.write(buffer);
        }

        GlobalGraph targetGraph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            assertThrows(CorruptedGraphBufferException.class, () -> {
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            }, "Expected CorruptedGraphBufferException due to file being too small.");
        }
    }
}
