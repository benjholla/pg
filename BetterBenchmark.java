import java.util.*;
import java.util.stream.*;

public class BetterBenchmark {
    static class Node {
        int id;
        Node(int id) { this.id = id; }
        public int id() { return id; }
    }

    public static void main(String[] args) {
        Set<Node> internalSet = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            internalSet.add(new BetterBenchmark.Node(i));
        }

        // Warmup stream
        for (int i = 0; i < 5000; i++) {
            int[] streamArr = internalSet.stream().mapToInt(Node::id).toArray();
        }

        // Warmup loop
        for (int i = 0; i < 5000; i++) {
            int[] loopArr = new int[internalSet.size()];
            int j = 0;
            for (Node node : internalSet) {
                loopArr[j++] = node.id();
            }
        }

        long startStream = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            int[] streamArr = internalSet.stream().mapToInt(Node::id).toArray();
        }
        long endStream = System.nanoTime();

        long startLoop = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            int[] loopArr = new int[internalSet.size()];
            int j = 0;
            for (Node node : internalSet) {
                loopArr[j++] = node.id();
            }
        }
        long endLoop = System.nanoTime();

        System.out.println("Node Stream time: " + (endStream - startStream) / 1000000.0 + " ms");
        System.out.println("Node Loop time: " + (endLoop - startLoop) / 1000000.0 + " ms");
    }
}
