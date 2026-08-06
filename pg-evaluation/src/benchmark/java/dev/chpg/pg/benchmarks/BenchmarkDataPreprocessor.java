package dev.chpg.pg.benchmarks;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.io.DirectGraphBufferReader;
import dev.chpg.pg.io.DirectGraphBufferWriter;

public class BenchmarkDataPreprocessor {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java BenchmarkDataPreprocessor <input.dgb> <output.dgb>");
            return;
        }

        Path inputPath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);

        System.out.println("Reading original graph from: " + inputPath);
        GlobalGraph graph = new GlobalGraph();
        try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            DirectGraphBufferReader.read(channel, graph, graph.factory(), graph.factory());
        }

        System.out.println("Watermarking functions...");
        int watermarkId = 0;

        for (Node node : graph.nodes()) {
            if (node.tags().contains("XCSG.Function")) {
                // Inject an invariant, structural identifier
                node.attributes().put("benchmark.watermark", AttributeValue.value(watermarkId));
                watermarkId++;
            }
        }

        System.out.println("Successfully watermarked " + watermarkId + " XCSG.Function nodes.");
        System.out.println("Writing preprocessed graph to: " + outputPath);

        try (FileChannel channel = FileChannel.open(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            DirectGraphBufferWriter.write(graph, channel);
        }

        System.out.println("Done! Use " + outputPath.getFileName() + " for all JMH benchmarks.");
    }
}
