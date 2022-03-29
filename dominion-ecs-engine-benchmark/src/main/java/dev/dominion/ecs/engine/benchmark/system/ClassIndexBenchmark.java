/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmark.system;

import dev.dominion.ecs.engine.benchmark.DominionBenchmark;
import dev.dominion.ecs.engine.system.ClassIndex;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ClassIndexBenchmark extends DominionBenchmark {
    Map<Class<?>, Integer> classHashMap;
    ClassIndex classIndex;
    ClassIndex classIndexFallback;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(new String[]{fetchBenchmarkName(ClassIndexBenchmark.class)}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".getIndex"}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".getIndexFallback"}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".getIndexOrAddClass"}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".addClass"}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".addClassFallback"}
//        org.openjdk.jmh.Main.main(new String[]{ClassIndexBenchmark.class.getName() + ".hashGet"}
        );
    }

    @Setup(Level.Iteration)
    public void setup() {
        classIndex = new ClassIndex();
        classIndex.addClass(C1.class);

        classIndexFallback = new ClassIndex();
        classIndexFallback.useUseFallbackMap();
        classIndexFallback.addClass(C1.class);

        classHashMap = new ConcurrentHashMap<>();
        classHashMap.put(C1.class, 1);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public int getIndex() {
        return classIndex.getIndex(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public int getIndexFallback() {
        return classIndexFallback.getIndex(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public int addClass() {
        return classIndex.addClass(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public int addClassFallback() {
        return classIndexFallback.addClass(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public int getIndexOrAddClass() {
        return classIndex.getIndexOrAddClass(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public Integer hashGet() {
        return classHashMap.get(C1.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Benchmark
    public Integer hashPut() {
        return classHashMap.put(C1.class, 1);
    }

    private static class C1 {
    }
}
