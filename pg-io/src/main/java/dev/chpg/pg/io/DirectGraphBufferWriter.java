package dev.chpg.pg.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.GraphElement;
import dev.chpg.pg.api.Node;

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
 * The chunking logic is strictly controlled by ensureSpace() to handle dynamic metadata sizes.
 */
public class DirectGraphBufferWriter {

    /** The magic header */
    public static final int MAGIC_HEADER = 0x44474201;

    /** The magic footer */
    public static final byte[] MAGIC_FOOTER = new byte[] {
        0x45, 0x4F, 0x46, 0x44, 0x47, 0x42
    };

    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024; // 8MB

    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_BOOLEAN = 1;
    private static final byte TYPE_INT = 2;
    private static final byte TYPE_LONG = 3;
    private static final byte TYPE_DOUBLE = 4;
    private static final byte TYPE_BYTE_ARRAY = 5;

    /**
     * Writes the given graph to the provided FileChannel using the default 8MB buffer size.
     * @param graph the graph to write
     * @param channel the file channel to write to
     * @throws IOException if an I/O error occurs
     */
    public static void write(Graph graph, FileChannel channel) throws IOException {
        write(graph, channel, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Writes the given graph to the provided FileChannel using the specified buffer size.
     * @param graph the graph to write
     * @param channel the file channel to write to
     * @param bufferSize the size of the write buffer
     * @throws IOException if an I/O error occurs
     */
    public static void write(Graph graph, FileChannel channel, int bufferSize) throws IOException {
        // Seize absolute control of the file pointer to prevent corruption.
        channel.truncate(0);
        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        Collection<? extends Node> nodes = graph.nodes();
        Collection<? extends Edge> edges = graph.edges();

        // 1. Pass 0: Harvesting
        Map<String, Integer> dictionary = new LinkedHashMap<>();
        harvestStrings(nodes, dictionary);
        harvestStrings(edges, dictionary);

        // 2. Write Header
        ensureSpace(12, channel, buffer);
        buffer.putInt(MAGIC_HEADER);
        buffer.putInt(nodes.size());
        buffer.putInt(edges.size());

        // 3. Write Dictionary Block
        writeDictionary(dictionary, channel, buffer);

        // 4. Nodes Block (Pass 1)
        writeNodes(nodes, dictionary, channel, buffer);

        // 5. Edges Block (Pass 2)
        writeEdges(edges, dictionary, channel, buffer);

        // 6. Write Footer
        writeFooter(channel, buffer);
    }

    private static void harvestStrings(Collection<? extends GraphElement> elements, Map<String, Integer> dictionary) {
        for (GraphElement element : elements) {
            for (String tag : element.tags()) {
                dictionary.putIfAbsent(tag, dictionary.size());
            }
            for (Map.Entry<String, AttributeValue> entry : element.attributes().entrySet()) {
                dictionary.putIfAbsent(entry.getKey(), dictionary.size());
                if (entry.getValue() instanceof AttributeValue.StringValue sv) {
                    dictionary.putIfAbsent(sv.value(), dictionary.size());
                }
            }
        }
    }

    private static void writeDictionary(Map<String, Integer> dictionary, FileChannel channel, ByteBuffer buffer) throws IOException {
        ensureSpace(4, channel, buffer);
        buffer.putInt(dictionary.size());

        for (String str : dictionary.keySet()) {
            byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
            ensureSpace(4 + strBytes.length, channel, buffer);
            buffer.putInt(strBytes.length);
            buffer.put(strBytes);
        }
    }

    private static void writeNodes(Collection<? extends Node> nodes, Map<String, Integer> dictionary, FileChannel channel, ByteBuffer buffer) throws IOException {
        for (Node node : nodes) {
            ensureSpace(4, channel, buffer);
            buffer.putInt(node.id());
            writeTags(node, dictionary, channel, buffer);
            writeAttributes(node, dictionary, channel, buffer);
        }
    }

    private static void writeEdges(Collection<? extends Edge> edges, Map<String, Integer> dictionary, FileChannel channel, ByteBuffer buffer) throws IOException {
        for (Edge edge : edges) {
            ensureSpace(12, channel, buffer);
            buffer.putInt(edge.id());
            buffer.putInt(edge.from().id());
            buffer.putInt(edge.to().id());
            writeTags(edge, dictionary, channel, buffer);
            writeAttributes(edge, dictionary, channel, buffer);
        }
    }

    private static void writeTags(GraphElement element, Map<String, Integer> dictionary, FileChannel channel, ByteBuffer buffer) throws IOException {
        ensureSpace(4, channel, buffer);
        buffer.putInt(element.tags().size());
        for (String tag : element.tags()) {
            ensureSpace(4, channel, buffer);
            buffer.putInt(dictionary.get(tag));
        }
    }

    private static void writeAttributes(GraphElement element, Map<String, Integer> dictionary, FileChannel channel, ByteBuffer buffer) throws IOException {
        ensureSpace(4, channel, buffer);
        buffer.putInt(element.attributes().size());

        for (Map.Entry<String, AttributeValue> entry : element.attributes().entrySet()) {
            ensureSpace(4, channel, buffer);
            buffer.putInt(dictionary.get(entry.getKey()));

            AttributeValue val = entry.getValue();
            if (val instanceof AttributeValue.StringValue sv) {
                ensureSpace(5, channel, buffer);
                buffer.put(TYPE_STRING);
                buffer.putInt(dictionary.get(sv.value()));
            } else if (val instanceof AttributeValue.BooleanValue bv) {
                ensureSpace(2, channel, buffer);
                buffer.put(TYPE_BOOLEAN);
                buffer.put((byte) (bv.value() ? 1 : 0));
            } else if (val instanceof AttributeValue.IntegerValue iv) {
                ensureSpace(5, channel, buffer);
                buffer.put(TYPE_INT);
                buffer.putInt(iv.value());
            } else if (val instanceof AttributeValue.LongValue lv) {
                ensureSpace(9, channel, buffer);
                buffer.put(TYPE_LONG);
                buffer.putLong(lv.value());
            } else if (val instanceof AttributeValue.DoubleValue dv) {
                ensureSpace(9, channel, buffer);
                buffer.put(TYPE_DOUBLE);
                buffer.putDouble(dv.value());
            } else if (val instanceof AttributeValue.ByteArrayValue bav) {
                byte[] data = bav.value();
                ensureSpace(5, channel, buffer);
                buffer.put(TYPE_BYTE_ARRAY);
                buffer.putInt(data.length);

                writeByteArrayTieredStrategy(data, channel, buffer);
            }
        }
    }

    /**
     * Executes a 3-tier strategy for writing byte arrays to maximize throughput and minimize memory copies.
     *
     * Tier 1: Fits directly in the current chunk space. We write it into the buffer to be flushed later.
     * Tier 2: Does not fit in remaining space, but fits within the total buffer capacity. We flush the chunk and write it.
     * Tier 3: Massive payload exceeding buffer capacity completely. We flush the chunk, wrap the byte array,
     *         and write it directly to the FileChannel, bypassing the 8MB buffer limit completely.
     */
    private static void writeByteArrayTieredStrategy(byte[] data, FileChannel channel, ByteBuffer buffer) throws IOException {
        if (data.length <= buffer.remaining()) {
            // Tier 1: Dump directly if it fits in the current chunk space
            buffer.put(data);
        } else if (data.length <= buffer.capacity()) {
            // Tier 2: Flush current chunk and dump if the payload fits in total capacity but not remaining space
            flushBuffer(channel, buffer);
            buffer.put(data);
        } else {
            // Tier 3: Flush chunk and wrap the array to write directly to the FileChannel
            flushBuffer(channel, buffer);
            ByteBuffer direct = ByteBuffer.wrap(data);
            while (direct.hasRemaining()) {
                channel.write(direct);
            }
        }
    }

    private static void writeFooter(FileChannel channel, ByteBuffer buffer) throws IOException {
        ensureSpace(MAGIC_FOOTER.length, channel, buffer);
        buffer.put(MAGIC_FOOTER);
        flushBuffer(channel, buffer);
    }

    /**
     * Guarantees that the buffer has at least requiredBytes of space remaining.
     * If not, it flushes the current contents to the channel.
     * This strictly controls the chunking logic to handle dynamic metadata sizes.
     */
    private static void ensureSpace(int requiredBytes, FileChannel channel, ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < requiredBytes) {
            flushBuffer(channel, buffer);
        }
    }

    private static void flushBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }
}
