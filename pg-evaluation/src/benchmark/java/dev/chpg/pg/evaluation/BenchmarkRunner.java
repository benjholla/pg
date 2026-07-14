package dev.chpg.pg.evaluation;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;

public class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
        String reportDir = "build/reports/benchmarks";
        new File(reportDir).mkdirs();

        Options opt = new OptionsBuilder()
                .include(".*Benchmark") // Runs all JMH benchmarks
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .forks(1)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .result(reportDir + "/jmh-benchmark-results.json")
                .build();

        new Runner(opt).run();
    }
}
