/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.engine.benchmark.collections;

import dev.dominion.engine.benchmark.DominionBenchmark;
import dev.dominion.ecs.engine.collections.ObjectArrayPool;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class ObjectArrayPoolBenchmark extends DominionBenchmark {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(ObjectArrayPoolBenchmark.class)}
        );
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static class PopNew extends DominionBenchmark {
        ObjectArrayPool pool;

        @Param(value = {"1000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(PopNew.class)}
            );
        }

        @Setup(Level.Invocation)
        public void setup() {
            pool = new ObjectArrayPool(LoggingSystem.Context.VERBOSE_TEST);
        }

        @Benchmark
        public void pop(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(pool.pop(2));
            }
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static class PopCached extends DominionBenchmark {
        ObjectArrayPool pool;
        Object[] input = new Object[2];

        @Param(value = {"1000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(PopCached.class)}
            );
        }

        @Setup(Level.Invocation)
        public void setup() {
            pool = new ObjectArrayPool(LoggingSystem.Context.VERBOSE_TEST);
            for (int i = 0; i < size; i++) {
                pool.push(input);
            }
        }

        @SuppressWarnings("UnusedReturnValue")
        @Benchmark
        public void pop(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(pool.pop(2));
            }
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static class Push extends DominionBenchmark {
        ObjectArrayPool pool;
        Object[] input = new Object[16];

        @Param(value = {"1000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Push.class)}
            );
        }

        @Setup(Level.Invocation)
        public void setup() {
            pool = new ObjectArrayPool(LoggingSystem.Context.VERBOSE_TEST);
        }

        @SuppressWarnings("UnusedReturnValue")
        @Benchmark
        public void push(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(pool.push(input));
            }
        }
    }
}
