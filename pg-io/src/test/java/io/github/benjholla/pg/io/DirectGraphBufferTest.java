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
