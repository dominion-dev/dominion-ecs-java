/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.collections;

import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import static dev.dominion.ecs.engine.collections.ChunkedPool.Identifiable;

public class ChunkedPoolBenchmark extends DominionBenchmark {
    private static final IdSchema ID_SCHEMA =
            new IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT, ConfigSystem.DEFAULT_CHUNK_COUNT_BIT);

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(ChunkedPoolBenchmark.class)}
        );
    }


    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class TenantNewId extends DominionBenchmark {
        ChunkedPool.Tenant<IntEntity> tenant;

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(TenantNewId.class)}
            );
        }

        @SuppressWarnings("resource")
        @Setup(Level.Invocation)
        public void setup() {
            tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.TEST).newTenant();
        }

        @SuppressWarnings("UnusedReturnValue")
        @Benchmark
        public void nextId(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(tenant.nextId());
            }
        }

        @TearDown(Level.Invocation)
        public void tearDown() {
            tenant.close();
        }
    }


    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class TenantReuseId extends DominionBenchmark {
        ChunkedPool.Tenant<IntEntity> tenant;

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(TenantReuseId.class)}
            );
        }

        @SuppressWarnings("resource")
        @Setup(Level.Iteration)
        public void setup() {
            tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.TEST).newTenant();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < size; i++) {
                tenant.freeId(i);
            }
        }

        @Benchmark
        public void nextId(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(tenant.nextId());
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            tenant.close();
        }
    }


    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class TenantFreeId extends DominionBenchmark {
        ChunkedPool.Tenant<IntEntity> tenant;

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(TenantFreeId.class)}
            );
        }

        @SuppressWarnings("resource")
        @Setup(Level.Iteration)
        public void setup() {
            tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.TEST).newTenant();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < size; i++) {
                tenant.nextId();
            }
        }

        @Benchmark
        public void freeId(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(tenant.freeId(i));
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            tenant.close();
        }
    }


    public static class TenantIterator extends DominionBenchmark {
        ChunkedPool.Tenant<Id> tenant;

        @Param(value = {"100000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(TenantIterator.class)}
            );
        }

        @SuppressWarnings("resource")
        @Setup(Level.Invocation)
        public void setupInvocation() {
            tenant = new ChunkedPool<Id>(ID_SCHEMA, LoggingSystem.Context.TEST).newTenant();
            for (int i = 0; i < size; i++) {
                tenant.register(tenant.nextId(), new Id(i, null, null), null);
            }
        }

        @Benchmark
        public void iterator(Blackhole bh) {
            Iterator<Id> iterator = tenant.iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next());
            }
        }

        @TearDown(Level.Invocation)
        public void tearDown() {
            tenant.close();
        }

        public record Id(int id, Identifiable prev, Identifiable next) implements Identifiable {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public int setId(int id) {
                return id;
            }

            @Override
            public Identifiable getPrev() {
                return prev;
            }

            @Override
            public Identifiable setPrev(Identifiable prev) {
                return prev;
            }

            @Override
            public Identifiable getNext() {
                return next;
            }

            @Override
            public Identifiable setNext(Identifiable next) {
                return next;
            }

            @Override
            public void setArray(Object[] array, int offset) {
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        }
    }
}
