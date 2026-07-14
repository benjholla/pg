package dev.chpg.pg.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

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
            SecurityException ex = assertThrows(SecurityException.class, () -> {
                DirectGraphBufferReader.read(channel, null, null, null);
            });
            assertTrue(ex.getMessage().contains("implies more bytes than exist"));
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
            SecurityException ex = assertThrows(SecurityException.class, () -> {
                DirectGraphBufferReader.read(channel, null, null, null);
            });
            assertTrue(ex.getMessage().contains("exceeds the 1MB ceiling limit"));
        }
    }
}
