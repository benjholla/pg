package io.github.benjholla.pg.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

/**
 * Writer for the DirectGraphBuffer (.dgb) serialization format.
 *
 * Rationale:
 * This format provides a blistering fast, bare-metal binary graph serializer
 * using java.nio.channels.FileChannel and ByteBuffer to maximize throughput
 * and maintain mechanical sympathy with the CPU cache.
 *
 * An 8MB chunk size is an exceptional sensible default. It perfectly hits the mechanical
 * sweet spot between minimizing operating system context switches and respecting CPU cache hierarchies.
 *
 * The chunking logic is split into strictly segregated `writeNodes()` and `writeEdges()`
 * methods. This segregates the strides (4 bytes for nodes, 12 bytes for edges) to avoid
 * branching overhead and safely hands off the ByteBuffer.
 */
public class DirectGraphBufferWriter {

    // Magic Header: 0x44 0x47 0x42 0x01 (DGB + Version 1)
    public static final int MAGIC_HEADER = 0x44474201;

    // Magic Footer: 0x45 0x4F 0x46 0x44 0x47 0x42 (EOFDGB)
    public static final byte[] MAGIC_FOOTER = new byte[] {
        0x45, 0x4F, 0x46, 0x44, 0x47, 0x42
    };

    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024; // 8MB

    /**
     * Writes the given graph to the provided FileChannel using the default 8MB buffer size.
     */
    public static void write(Graph graph, FileChannel channel) throws IOException {
        write(graph, channel, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Writes the given graph to the provided FileChannel using the specified buffer size.
     */
    public static void write(Graph graph, FileChannel channel, int bufferSize) throws IOException {
        // Seize absolute control of the file pointer to prevent corruption.
        channel.truncate(0);
        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        Collection<? extends Node> nodes = graph.nodes();
        Collection<? extends Edge> edges = graph.edges();

        // 1. Write Header
        buffer.putInt(MAGIC_HEADER);
        buffer.putInt(nodes.size());
        buffer.putInt(edges.size());

        // 2. Nodes Block
        writeNodes(nodes, channel, buffer);

        // 3. Edges Block
        writeEdges(edges, channel, buffer);

        // 4. Write Footer
        writeFooter(channel, buffer);
    }

    private static void writeNodes(Collection<? extends Node> nodes, FileChannel channel, ByteBuffer buffer) throws IOException {
        for (Node node : nodes) {
            if (buffer.remaining() < 4) {
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
            buffer.putInt(node.id());
        }
    }

    private static void writeEdges(Collection<? extends Edge> edges, FileChannel channel, ByteBuffer buffer) throws IOException {
        for (Edge edge : edges) {
            // 12 bytes required for 3 ints
            if (buffer.remaining() < 12) {
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
            buffer.putInt(edge.id());
            buffer.putInt(edge.from().id());
            buffer.putInt(edge.to().id());
        }
    }

    private static void writeFooter(FileChannel channel, ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < MAGIC_FOOTER.length) {
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        }
        buffer.put(MAGIC_FOOTER);

        // Flush any remaining data in the buffer
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}
