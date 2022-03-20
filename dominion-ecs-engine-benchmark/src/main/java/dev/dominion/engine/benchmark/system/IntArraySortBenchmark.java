/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.engine.benchmark.system;

import dev.dominion.ecs.engine.collections.IntArraySort;
import dev.dominion.engine.benchmark.DominionBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class IntArraySortBenchmark extends DominionBenchmark {

    int[] inArr02 = new int[]{1, 0};
    int[] inArr04 = new int[]{3, 2, 1, 0};
    int[] inArr08 = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
    int[] inArr16 = new int[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(IntArraySortBenchmark.class)}
        );
    }

    @Benchmark
    public int[] sort02() {
        return IntArraySort.sort(inArr02, 2);
    }

    @Benchmark
    public int[] sort04() {
        return IntArraySort.sort(inArr04, 4);
    }

    @Benchmark
    public int[] sort08() {
        return IntArraySort.sort(inArr08, 8);
    }

    @Benchmark
    public int[] sort16() {
        return IntArraySort.sort(inArr16, 16);
    }
}
