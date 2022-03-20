/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.engine.benchmark.collections;

import dev.dominion.engine.benchmark.DominionBenchmark;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SparseIntMapBenchmark extends DominionBenchmark {

    SparseIntMap<Integer> sparseMap = new SparseIntMap<>();
    Map<Integer, Integer> hashMap = new ConcurrentHashMap<>();
    Iterator<Integer> sparseIterator;
    Iterator<Integer> hashIterator;

    @Param(value = {"1000"})
    int size;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(SparseIntMapBenchmark.class)}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".sparse"}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".hash"}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".sparsePut"}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".sparseIterator"}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".hashPut"}
//                new String[]{ConcurrentIntMapBenchmark.class.getName() + ".sorted"}
        );
    }

    @Setup(Level.Iteration)
    public void setup() {
        for (int i = size - 1; i >= 0; i--) {
            sparseMap.put(i, i);
            hashMap.put(i, i);
        }
        sparseIterator = sparseMap.iterator();
        hashIterator = hashMap.values().iterator();
    }


    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public Integer hashGet() {
        return hashMap.get(5);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public Integer hashPut() {
        return hashMap.put(10, 100);
    }

    @Benchmark
    public void hashIterator(Blackhole bh) {
        while (hashIterator.hasNext()) {
            bh.consume(hashIterator.next());
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public Integer sparseGet() {
        return sparseMap.get(5);
    }

    @Benchmark
    public Integer sparsePut() {
        return sparseMap.put(10, 100);
    }

    @Benchmark
    public void sparseIterator(Blackhole bh) {
        while (sparseIterator.hasNext()) {
            bh.consume(sparseIterator.next());
        }
    }
}
