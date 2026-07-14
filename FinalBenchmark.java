import java.util.*;

public class FinalBenchmark {
    public static void main(String[] args) {
        Map<Integer, Object> edges = new HashMap<>();
        for (int i = 0; i < 1000; i++) edges.put(i, null);

        long dummy1 = 0, dummy2 = 0;

        // Warmup
        for (int i = 0; i < 50000; i++) {
            dummy1 += edges.keySet().stream().mapToInt(Integer::intValue).toArray().length;

            int[] result = new int[edges.size()];
            int j = 0;
            for (Integer id : edges.keySet()) {
                result[j++] = id;
            }
            dummy2 += result.length;
        }

        long t1 = System.nanoTime();
        for (int i = 0; i < 50000; i++) {
            dummy1 += edges.keySet().stream().mapToInt(Integer::intValue).toArray().length;
        }
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        for (int i = 0; i < 50000; i++) {
            int[] result = new int[edges.size()];
            int j = 0;
            for (Integer id : edges.keySet()) {
                result[j++] = id;
            }
            dummy2 += result.length;
        }
        long t4 = System.nanoTime();

        System.out.println("Stream: " + (t2 - t1)/1000000.0 + " ms");
        System.out.println("Loop:   " + (t4 - t3)/1000000.0 + " ms");
        System.out.println(dummy1 + " " + dummy2);
    }
}
