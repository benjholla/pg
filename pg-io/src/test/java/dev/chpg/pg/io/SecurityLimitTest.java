package dev.chpg.pg.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.global.GlobalGraph;

// In pg-io, we use mock factories or anonymous classes to avoid specific backend references like GlobalGraph.
// However, the test only checks the first failure in read(), so passing nulls is enough if it fails early!
// Wait, `read` checks if nodeFactory and edgeFactory are null... well let's just supply nulls and see if it fails at dictionary size first!
public class SecurityLimitTest {

    @Test
    public void testMaliciousDictionarySize() throws Exception {
        File tempFile = File.createTempFile("malicious-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(16);
            header.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            header.putInt(0); // totalNodes
            header.putInt(0); // totalEdges
            header.putInt(Integer.MAX_VALUE / 8); // MALICIOUS Dictionary Size
            header.flip();
            channel.write(header);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("implies more bytes than exist"));
        }
    }

    @Test
    public void testNegativeDictionarySize() throws Exception {
        File tempFile = File.createTempFile("negative-dict-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(16);
            header.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            header.putInt(0); // totalNodes
            header.putInt(0); // totalEdges
            header.putInt(-1); // NEGATIVE Dictionary Size
            header.flip();
            channel.write(header);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("Dictionary size cannot be negative: -1"));
        }
    }

    @Test
    public void testNegativeStringLength() throws Exception {
        File tempFile = File.createTempFile("negative-strlen-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(20);
            header.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            header.putInt(0); // totalNodes
            header.putInt(0); // totalEdges
            header.putInt(1); // 1 dictionary string
            header.putInt(-1); // NEGATIVE String Length
            header.flip();
            channel.write(header);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("String length cannot be negative: -1"));
        }
    }

    @Test
    public void testStringLengthExceedsAvailableBytes() throws Exception {
        File tempFile = File.createTempFile("huge-strlen-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(20);
            header.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            header.putInt(0); // totalNodes
            header.putInt(0); // totalEdges
            header.putInt(1); // 1 dictionary string
            header.putInt(100); // 100 bytes, but no actual string data
            header.flip();
            channel.write(header);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("exceeds available bytes in the physical file"));
        }
    }

    @Test
    public void testMaliciousStringLength() throws Exception {
        File tempFile = File.createTempFile("malicious-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(20);
            header.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            header.putInt(0); // totalNodes
            header.putInt(0); // totalEdges
            header.putInt(1); // 1 dictionary string
            header.putInt(1024 * 1024 + 1); // MALICIOUS String Length
            header.flip();
            channel.write(header);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("exceeds the 1MB ceiling limit"));
        }
    }

    @Test
    public void testNegativeByteArrayLength() throws Exception {
        File tempFile = File.createTempFile("negative-bytearray-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(100);
            buffer.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            buffer.putInt(1); // totalNodes
            buffer.putInt(0); // totalEdges
            buffer.putInt(1); // dictionary size
            buffer.putInt(4); // dict str len
            buffer.put("attr".getBytes()); // dict string

            // Nodes pass
            buffer.putInt(0); // node id
            buffer.putInt(0); // tag count
            buffer.putInt(1); // attr count
            buffer.putInt(0); // attr key dict id
            buffer.put((byte) 5); // TYPE_BYTE_ARRAY marker
            buffer.putInt(-1); // NEGATIVE byte array len
            buffer.flip();
            channel.write(buffer);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("Byte array length cannot be negative: -1"));
        }
    }

    @Test
    public void testMaliciousByteArrayLength() throws Exception {
        File tempFile = File.createTempFile("malicious-bytearray-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(100);
            buffer.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            buffer.putInt(1); // totalNodes
            buffer.putInt(0); // totalEdges
            buffer.putInt(1); // dictionary size
            buffer.putInt(4); // dict str len
            buffer.put("attr".getBytes()); // dict string

            // Nodes pass
            buffer.putInt(0); // node id
            buffer.putInt(0); // tag count
            buffer.putInt(1); // attr count
            buffer.putInt(0); // attr key dict id
            buffer.put((byte) 5); // TYPE_BYTE_ARRAY marker
            buffer.putInt(16 * 1024 * 1024 + 1); // MALICIOUS byte array len (> 16MB)
            buffer.flip();
            channel.write(buffer);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(channel, targetGraph, targetGraph.factory(), targetGraph.factory());
            });
            assertTrue(ex.getMessage().contains("exceeds the 16MB ceiling limit"));
        }
    }


    @Test
    public void testUnexpectedEndOfFileWhileReadingByteArray() throws Exception {
        File tempFile = File.createTempFile("eof-bytearray-dgb", ".dgb");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(100);
            buffer.putInt(DirectGraphBufferWriter.MAGIC_HEADER);
            buffer.putInt(1); // totalNodes
            buffer.putInt(0); // totalEdges
            buffer.putInt(1); // dictionary size
            buffer.putInt(4); // dict str len
            buffer.put("attr".getBytes()); // dict string

            // Nodes pass
            buffer.putInt(0); // node id
            buffer.putInt(0); // tag count
            buffer.putInt(1); // attr count
            buffer.putInt(0); // attr key dict id
            buffer.put((byte) 5); // TYPE_BYTE_ARRAY marker
            buffer.putInt(15); // len = 15, we only have 11 bytes remaining including footer
            buffer.put(new byte[] { 1, 2, 3, 4, 5 });
            buffer.flip();
            channel.write(buffer);

            ByteBuffer footer = ByteBuffer.allocate(DirectGraphBufferWriter.MAGIC_FOOTER.length);
            footer.put(DirectGraphBufferWriter.MAGIC_FOOTER);
            footer.flip();
            channel.write(footer);
        }

        try (FileChannel realChannel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ)) {
            FileChannel fakeChannel = new FileChannel() {
                private boolean fakeSize = false;
                @Override public int read(ByteBuffer dst) throws IOException { return realChannel.read(dst); }
                @Override public long read(ByteBuffer[] dsts, int offset, int length) throws IOException { return realChannel.read(dsts, offset, length); }
                @Override public int write(ByteBuffer src) throws IOException { return realChannel.write(src); }
                @Override public long write(ByteBuffer[] srcs, int offset, int length) throws IOException { return realChannel.write(srcs, offset, length); }
                @Override public long position() throws IOException { return realChannel.position(); }
                @Override public FileChannel position(long newPosition) throws IOException {
                    realChannel.position(newPosition);
                    // Start faking size after we read footer and rewind to 0
                    if (newPosition == 0) {
                        fakeSize = true;
                    }
                    return this;
                }
                @Override public long size() throws IOException { return realChannel.size() + (fakeSize ? 100 : 0); } // FAKE SIZE ONLY DURING READ!
                @Override public FileChannel truncate(long size) throws IOException { return realChannel.truncate(size); }
                @Override public void force(boolean metaData) throws IOException { realChannel.force(metaData); }
                @Override public long transferTo(long position, long count, WritableByteChannel target) throws IOException { return realChannel.transferTo(position, count, target); }
                @Override public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException { return realChannel.transferFrom(src, position, count); }
                @Override public int read(ByteBuffer dst, long position) throws IOException { return realChannel.read(dst, position); }
                @Override public int write(ByteBuffer src, long position) throws IOException { return realChannel.write(src, position); }
                @Override public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException { return realChannel.map(mode, position, size); }
                @Override public FileLock lock(long position, long size, boolean shared) throws IOException { return realChannel.lock(position, size, shared); }
                @Override public FileLock tryLock(long position, long size, boolean shared) throws IOException { return realChannel.tryLock(position, size, shared); }
                @Override protected void implCloseChannel() throws IOException { realChannel.close(); }
            };

            dev.chpg.pg.io.CorruptedGraphBufferException ex = assertThrows(dev.chpg.pg.io.CorruptedGraphBufferException.class, () -> {
                GlobalGraph targetGraph = new GlobalGraph();
                DirectGraphBufferReader.read(fakeChannel, targetGraph, targetGraph.factory(), targetGraph.factory(), 12);
            });
            assertTrue(ex.getMessage().contains("Unexpected end of file while reading byte array"));
        }
    }
}
