/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.system;

import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import dev.dominion.ecs.engine.system.Logging;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LoggingBenchmark extends DominionBenchmark {


    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(LoggingBenchmark.class)}
        );
    }

    @Benchmark
    public boolean isLoggable() {
        return Logging.isLoggable(-1, System.Logger.Level.DEBUG);
    }
}
