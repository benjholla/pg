package dev.chpg.pg.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeFactory;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.GraphElement;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeFactory;

/**
 * Reader for the DirectGraphBuffer (.dgb) serialization format.
 *
 * Rationale:
 * This format provides a blistering fast, bare-metal binary graph deserializer.
 *
 * An 8MB chunk size perfectly hits the mechanical sweet spot, allowing the Linux kernel
 * read-ahead prefetcher to run flawlessly while remaining within the CPU L3 cache.
 *
 * The chunking logic is strictly controlled by ensureBytes() to handle dynamic metadata sizes.
 */
public class DirectGraphBufferReader {

    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024; // 8MB

    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_BOOLEAN = 1;
    private static final byte TYPE_INT = 2;
    private static final byte TYPE_LONG = 3;
    private static final byte TYPE_DOUBLE = 4;
    private static final byte TYPE_BYTE_ARRAY = 5;

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

        ensureBytes(12, channel, buffer);

        int header = buffer.getInt();
        if (header != DirectGraphBufferWriter.MAGIC_HEADER) {
            throw new CorruptedGraphBufferException(
                String.format("Invalid magic header. Expected 0x%08X but found 0x%08X. This is not a valid DirectGraphBuffer file.",
                              DirectGraphBufferWriter.MAGIC_HEADER, header)
            );
        }

        int totalNodes = buffer.getInt();
        int totalEdges = buffer.getInt();

        // 3. Read Dictionary
        String[] dictionary = readDictionary(channel, buffer);

        // Pre-allocate custom IntIntMap
        IntIntMap translationMap = new IntIntMap(totalNodes);

        // 4. Node Phase (Pass 1)
        readNodes(channel, buffer, targetGraph, nodeFactory, totalNodes, dictionary, translationMap);

        // 5. Edge Phase (Pass 2)
        readEdges(channel, buffer, targetGraph, edgeFactory, totalEdges, dictionary, translationMap);
    }

    private static String[] readDictionary(FileChannel channel, ByteBuffer buffer) throws IOException {
        ensureBytes(4, channel, buffer);
        int dictionarySize = buffer.getInt();

        long remainingBytes = buffer.remaining() + (channel.size() - channel.position());
        if (dictionarySize < 0) {
            throw new CorruptedGraphBufferException("Dictionary size cannot be negative: " + dictionarySize);
        }
        if (dictionarySize * 4L > remainingBytes) {
            throw new CorruptedGraphBufferException("Dictionary size " + dictionarySize + " implies more bytes than exist in the physical file.");
        }

        String[] dictionary = new String[dictionarySize];

        for (int i = 0; i < dictionarySize; i++) {
            ensureBytes(4, channel, buffer);
            int strLen = buffer.getInt();

            if (strLen < 0) {
                throw new CorruptedGraphBufferException("String length cannot be negative: " + strLen);
            }
            if (strLen > 1024 * 1024) {
                throw new CorruptedGraphBufferException("String length " + strLen + " bytes exceeds the 1MB ceiling limit.");
            }
            // Optimization: avoid channel.size() and channel.position() syscalls on every loop iteration
            // We know we can read up to `strLen` bytes safely without querying the channel size again
            // if we just verify the total bytes we've read so far vs the total file size (which is an
            // optimization we can apply by checking remainingBytes when it doesn't fit in the buffer).
            // Actually, a simpler way is just to check if strLen > buffer.remaining() + channel.size() - channel.position()
            // ONLY if strLen > buffer.remaining(), because if it fits in the buffer, it must be safe!
            if (strLen > buffer.remaining()) {
                long currentRemainingBytes = buffer.remaining() + (channel.size() - channel.position());
                if (strLen > currentRemainingBytes) {
                    throw new CorruptedGraphBufferException("String length " + strLen + " exceeds available bytes in the physical file.");
                }
            }

            ensureBytes(strLen, channel, buffer);
            byte[] strBytes = new byte[strLen];
            buffer.get(strBytes);
            dictionary[i] = new String(strBytes, StandardCharsets.UTF_8);
        }
        return dictionary;
    }

    private static void readNodes(FileChannel channel, ByteBuffer buffer, Graph targetGraph, NodeFactory nodeFactory, int totalNodes, String[] dictionary, IntIntMap translationMap) throws IOException {
        for (int i = 0; i < totalNodes; i++) {
            ensureBytes(4, channel, buffer);
            int fileNodeId = buffer.getInt();

            Node newNode = nodeFactory.createNode();

            readTagsAndAttributes(channel, buffer, newNode, dictionary, fileNodeId);

            targetGraph.addNode(newNode);
            translationMap.put(fileNodeId, newNode.id());
        }
    }

    private static void readEdges(FileChannel channel, ByteBuffer buffer, Graph targetGraph, EdgeFactory edgeFactory, int totalEdges, String[] dictionary, IntIntMap translationMap) throws IOException {
        for (int i = 0; i < totalEdges; i++) {
            ensureBytes(12, channel, buffer);
            int fileEdgeId = buffer.getInt();
            int fileFromId = buffer.getInt();
            int fileToId = buffer.getInt();

            int actualFromId = translationMap.get(fileFromId);
            if (actualFromId == -1) {
                throw new CorruptedGraphBufferException("Edge ID " + fileEdgeId + " references missing source node ID: " + fileFromId);
            }

            int actualToId = translationMap.get(fileToId);
            if (actualToId == -1) {
                throw new CorruptedGraphBufferException("Edge ID " + fileEdgeId + " references missing target node ID: " + fileToId);
            }

            Optional<Node> fromNodeOpt = targetGraph.node(actualFromId);
            Optional<Node> toNodeOpt = targetGraph.node(actualToId);

            if (!fromNodeOpt.isPresent() || !toNodeOpt.isPresent()) {
                throw new CorruptedGraphBufferException("Edge references non-existent node ID.");
            }

            Edge newEdge = edgeFactory.createEdge(fromNodeOpt.get(), toNodeOpt.get());

            readTagsAndAttributes(channel, buffer, newEdge, dictionary, fileEdgeId);

            targetGraph.addEdge(newEdge);
        }
    }

    private static void readTagsAndAttributes(FileChannel channel, ByteBuffer buffer, GraphElement element, String[] dictionary, int elementId) throws IOException {
        ensureBytes(4, channel, buffer);
        int tagCount = buffer.getInt();
        for (int j = 0; j < tagCount; j++) {
            ensureBytes(4, channel, buffer);
            int dictId = buffer.getInt();
            if (dictId < 0 || dictId >= dictionary.length) {
                throw new CorruptedGraphBufferException("Dictionary ID out of bounds: " + dictId + ". The dictionary only contains " + dictionary.length + " entries. The .dgb file is corrupted while parsing tags for element ID: " + elementId);
            }
            element.tags().add(dictionary[dictId]);
        }

        ensureBytes(4, channel, buffer);
        int attrCount = buffer.getInt();
        for (int j = 0; j < attrCount; j++) {
            ensureBytes(5, channel, buffer); // key dictId (4) + type marker (1)
            int keyDictId = buffer.getInt();
            if (keyDictId < 0 || keyDictId >= dictionary.length) {
                throw new CorruptedGraphBufferException("Dictionary ID out of bounds: " + keyDictId + ". The dictionary only contains " + dictionary.length + " entries. The .dgb file is corrupted while parsing attributes for element ID: " + elementId);
            }
            String key = dictionary[keyDictId];

            byte marker = buffer.get();
            AttributeValue attrValue;

            if (marker == TYPE_STRING) {
                ensureBytes(4, channel, buffer);
                int valDictId = buffer.getInt();
                if (valDictId < 0 || valDictId >= dictionary.length) {
                    throw new CorruptedGraphBufferException("Dictionary ID out of bounds: " + valDictId + ". The dictionary only contains " + dictionary.length + " entries. The .dgb file is corrupted while parsing attributes for element ID: " + elementId);
                }
                attrValue = AttributeValue.value(dictionary[valDictId]);
            } else if (marker == TYPE_BOOLEAN) {
                ensureBytes(1, channel, buffer);
                byte b = buffer.get();
                attrValue = AttributeValue.value(b != 0);
            } else if (marker == TYPE_INT) {
                ensureBytes(4, channel, buffer);
                attrValue = AttributeValue.value(buffer.getInt());
            } else if (marker == TYPE_LONG) {
                ensureBytes(8, channel, buffer);
                attrValue = AttributeValue.value(buffer.getLong());
            } else if (marker == TYPE_DOUBLE) {
                ensureBytes(8, channel, buffer);
                attrValue = AttributeValue.value(buffer.getDouble());
            } else if (marker == TYPE_BYTE_ARRAY) {
                ensureBytes(4, channel, buffer);
                int len = buffer.getInt();
                byte[] data = readByteArrayTieredStrategy(len, channel, buffer, elementId);
                attrValue = AttributeValue.value(data);
            } else {
                throw new CorruptedGraphBufferException("Invalid attribute type marker detected: " + marker + ". Expected a value between 0 and 5 while parsing attributes for GraphElement ID: " + elementId);
            }

            element.attributes().put(key, attrValue);
        }
    }

    /**
     * Reads a byte array using a chunked strategy.
     * Continuously pulls from the buffer and refills it until the entire array is read,
     * cleanly handling data boundaries that span across chunks.
     */
    private static byte[] readByteArrayTieredStrategy(int len, FileChannel channel, ByteBuffer buffer, int elementId) throws IOException {
        long remainingBytes = buffer.remaining() + (channel.size() - channel.position());
        if (len < 0) {
            throw new CorruptedGraphBufferException("Byte array length cannot be negative: " + len + " for element ID: " + elementId);
        }
        if (len > 16 * 1024 * 1024) {
            throw new CorruptedGraphBufferException("Byte array length " + len + " bytes exceeds the 16MB ceiling limit for element ID: " + elementId);
        }
        if (len > remainingBytes) {
            throw new CorruptedGraphBufferException("Byte array length " + len + " exceeds available bytes in the physical file for element ID: " + elementId);
        }

        byte[] data = new byte[len];
        int offset = 0;
        while (offset < len) {
            int remainingInBuffer = buffer.remaining();
            if (remainingInBuffer == 0) {
                compactAndRefill(channel, buffer);
                remainingInBuffer = buffer.remaining();
                if (remainingInBuffer == 0) {
                    throw new CorruptedGraphBufferException("Unexpected end of file while reading byte array for element ID: " + elementId);
                }
            }
            int bytesToRead = Math.min(len - offset, remainingInBuffer);
            buffer.get(data, offset, bytesToRead);
            offset += bytesToRead;
        }
        return data;
    }

    /**
     * Guarantees that the buffer has at least requiredBytes available for reading.
     * If not, it compacts the remaining unread bytes to the front and refills the buffer from the channel.
     * This strictly controls the chunking logic to handle dynamic metadata sizes without clearing state.
     */
    private static void ensureBytes(int requiredBytes, FileChannel channel, ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < requiredBytes) {
            compactAndRefill(channel, buffer);
            if (buffer.remaining() < requiredBytes) {
                // If the channel has reached EOF and we STILL don't have enough bytes,
                // it is mathematically guaranteed that the file was truncated mid-payload.
                throw new CorruptedGraphBufferException(
                    "Unexpected end of file. Required " + requiredBytes + " bytes but only " +
                    buffer.remaining() + " were available. The .dgb file is truncated."
                );
            }
        }
    }

    private static void fillBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining() && channel.read(buffer) != -1) {
            // read until buffer is full or EOF
        }
        buffer.flip();
    }

    /**
     * Preserves unread bytes across read boundaries by compacting them to the beginning of the buffer,
     * then refilling the remaining capacity from the file channel.
     */
    private static void compactAndRefill(FileChannel channel, ByteBuffer buffer) throws IOException {
        buffer.compact();
        while (buffer.hasRemaining() && channel.read(buffer) != -1) {
             // read until buffer is full or EOF
        }
        buffer.flip();
    }
}
