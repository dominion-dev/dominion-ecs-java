/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.system;

import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import dev.dominion.ecs.engine.system.IndexKey;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class IndexKeyBenchmark extends DominionBenchmark {

    IndexKey indexKey;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(IndexKeyBenchmark.class)}
        );
    }

    @Setup(Level.Iteration)
    public void setup() {
        indexKey = new IndexKey(
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160}
        );
    }


    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public IndexKey newIndexKey() {
        return new IndexKey(
                new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160}
        );
    }

    @SuppressWarnings("UnusedReturnValue")
//    @Benchmark
    public int hashCode() {
        return indexKey.hashCode();
    }
}
