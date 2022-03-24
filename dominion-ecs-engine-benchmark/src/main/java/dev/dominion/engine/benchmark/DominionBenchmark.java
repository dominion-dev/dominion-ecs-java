/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.engine.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(value = 1, warmups = 1)
@State(Scope.Thread)
public class DominionBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    public static String fetchBenchmarkName(Class<?> benchmarkClass) {
        return benchmarkClass.getName().replace('$', '.');
    }

    public static final class All {
        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{DominionBenchmark.class.getPackageName()}
            );
        }
    }

    public static final class Functional {
        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{
                            fetchBenchmarkName(EntityRepositoryBenchmark.CreateEntity.class)
                            , fetchBenchmarkName(EntityRepositoryBenchmark.DeleteEntity.class)
                            , fetchBenchmarkName(EntityRepositoryBenchmark.FindComponents.class)
                            , fetchBenchmarkName(EntityBenchmark.Add.class)
                            , fetchBenchmarkName(EntityBenchmark.Remove.class)
                            , fetchBenchmarkName(EntityBenchmark.SetState.class)
                            , fetchBenchmarkName(EntityBenchmark.SetEnabled.class)
                            , fetchBenchmarkName(EntityBenchmark.Has.class)
                            , fetchBenchmarkName(EntityBenchmark.Contains.class)
                    }
            );
        }
    }
}
