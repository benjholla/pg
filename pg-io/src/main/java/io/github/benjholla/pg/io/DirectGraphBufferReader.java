package io.github.benjholla.pg.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Optional;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeFactory;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeFactory;

/**
 * Reader for the DirectGraphBuffer (.dgb) serialization format.
 *
 * Rationale:
 * This format provides a blistering fast, bare-metal binary graph deserializer.
 *
 * An 8MB chunk size perfectly hits the mechanical sweet spot, allowing the Linux kernel
 * read-ahead prefetcher to run flawlessly while remaining within the CPU L3 cache.
 *
 * The chunking logic is strictly segregated into `readNodes()` and `readEdges()` helper
 * methods. Because they consume memory at entirely different mathematical strides (4 bytes vs 12 bytes),
 * segregating them keeps the state machine clean. The exact same ByteBuffer instance is
 * safely handed between them without clearing or resetting position, avoiding the handoff trap.
 */
public class DirectGraphBufferReader {

    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024; // 8MB

    /**
     * Reads a DirectGraphBuffer from the channel into the target graph using the default 8MB buffer size.
     */
    public static void read(FileChannel channel, Graph targetGraph, NodeFactory nodeFactory, EdgeFactory edgeFactory) throws IOException {
        read(channel, targetGraph, nodeFactory, edgeFactory, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Reads a DirectGraphBuffer from the channel into the target graph using the specified buffer size.
     */
    public static void read(FileChannel channel, Graph targetGraph, NodeFactory nodeFactory, EdgeFactory edgeFactory, int bufferSize) throws IOException {
        // 1. Footer Validation (Jump to end)
        long fileSize = channel.size();
        if (fileSize < 10) { // 4(header) + 6(footer) = 10 bytes min
            throw new CorruptedGraphBufferException("File is too small to be a valid DirectGraphBuffer.");
        }

        channel.position(fileSize - 6);
        ByteBuffer footerBuffer = ByteBuffer.allocate(6);
        int readBytes = channel.read(footerBuffer);
        if (readBytes != 6) {
            throw new CorruptedGraphBufferException("Failed to read magic footer.");
        }

        if (!Arrays.equals(footerBuffer.array(), DirectGraphBufferWriter.MAGIC_FOOTER)) {
            throw new CorruptedGraphBufferException("Invalid magic footer. Corrupted or incomplete DirectGraphBuffer.");
        }

        // 2. Rewind and Read Header
        channel.position(0);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.clear();
        fillBuffer(channel, buffer);

        if (buffer.remaining() < 12) {
            throw new CorruptedGraphBufferException("File is too small to contain a valid header.");
        }

        int header = buffer.getInt();
        if (header != DirectGraphBufferWriter.MAGIC_HEADER) {
            throw new CorruptedGraphBufferException("Invalid magic header. Not a DirectGraphBuffer.");
        }

        int totalNodes = buffer.getInt();
        int totalEdges = buffer.getInt();

        // Pre-allocate custom IntIntMap
        IntIntMap translationMap = new IntIntMap(totalNodes);

        // 3. Node Phase (Pass 1)
        readNodes(channel, buffer, targetGraph, nodeFactory, totalNodes, translationMap);

        // 4. Edge Phase (Pass 2)
        readEdges(channel, buffer, targetGraph, edgeFactory, totalEdges, translationMap);
    }

    private static void readNodes(FileChannel channel, ByteBuffer buffer, Graph targetGraph, NodeFactory nodeFactory, int totalNodes, IntIntMap translationMap) throws IOException {
        for (int i = 0; i < totalNodes; i++) {
            // Check stride for node (4 bytes)
            if (buffer.remaining() < 4) {
                compactAndRefill(channel, buffer);
                if (buffer.remaining() < 4) {
                    throw new CorruptedGraphBufferException("Unexpected end of file while reading nodes.");
                }
            }

            int fileNodeId = buffer.getInt();
            Node newNode = nodeFactory.createNode();
            targetGraph.addNode(newNode);
            translationMap.put(fileNodeId, newNode.id());
        }
    }

    private static void readEdges(FileChannel channel, ByteBuffer buffer, Graph targetGraph, EdgeFactory edgeFactory, int totalEdges, IntIntMap translationMap) throws IOException {
        for (int i = 0; i < totalEdges; i++) {
            // Check stride for edge (12 bytes)
            if (buffer.remaining() < 12) {
                compactAndRefill(channel, buffer);
                if (buffer.remaining() < 12) {
                    throw new CorruptedGraphBufferException("Unexpected end of file while reading edges.");
                }
            }

            int fileEdgeId = buffer.getInt();
            int fileFromId = buffer.getInt();
            int fileToId = buffer.getInt();

            int actualFromId = translationMap.get(fileFromId);
            int actualToId = translationMap.get(fileToId);

            Optional<Node> fromNodeOpt = targetGraph.node(actualFromId);
            Optional<Node> toNodeOpt = targetGraph.node(actualToId);

            if (!fromNodeOpt.isPresent() || !toNodeOpt.isPresent()) {
                throw new CorruptedGraphBufferException("Edge references non-existent node ID.");
            }

            Edge newEdge = edgeFactory.createEdge(fromNodeOpt.get(), toNodeOpt.get());
            targetGraph.addEdge(newEdge);
        }
    }

    private static void fillBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining() && channel.read(buffer) != -1) {
            // read until buffer is full or EOF
        }
        buffer.flip();
    }

    private static void compactAndRefill(FileChannel channel, ByteBuffer buffer) throws IOException {
        buffer.compact();
        while (buffer.hasRemaining() && channel.read(buffer) != -1) {
             // read until buffer is full or EOF
        }
        buffer.flip();
    }
}
