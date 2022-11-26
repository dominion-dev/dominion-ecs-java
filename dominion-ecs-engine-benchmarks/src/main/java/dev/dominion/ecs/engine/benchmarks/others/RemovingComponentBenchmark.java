/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.others;

import com.artemis.*;
import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import dev.dominion.ecs.engine.benchmarks.EntityBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class RemovingComponentBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(RemovingComponentBenchmark.Dominion.class),
//                        DominionBenchmark.fetchBenchmarkName(RemovingComponentBenchmark.Dominion.RemoveFrom06.class),
                        DominionBenchmark.fetchBenchmarkName(RemovingComponentBenchmark.Artemis.class)
                }
        );
    }

    public static class Dominion {
        record C1(int id) {
        }

        record C2(int id) {
        }

        record C3(int id) {
        }

        record C4(int id) {
        }

        record C5(int id) {
        }

        record C6(int id) {
        }

        @BenchmarkMode(Mode.Throughput)
        public static class RemoveFrom01 extends EntityBenchmark.EntityMethodBenchmark {
            Object[] input = new Object[]{new C1(0)};

            @Benchmark
            public void remove(Blackhole bh) {
                for (int i = 0; i < size; i++) {
                    bh.consume(entities[i].removeType(C1.class));
                }
            }

            public Object[] getInput() {
                return input;
            }
        }

        public static class RemoveFrom02 extends RemoveFrom01 {
            Object[] input = new Object[]{new C1(0), new C2(0)};

            public Object[] getInput() {
                return input;
            }
        }

        public static class RemoveFrom04 extends RemoveFrom01 {
            Object[] input = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};

            public Object[] getInput() {
                return input;
            }
        }

        public static class RemoveFrom06 extends RemoveFrom01 {
            Object[] input = new Object[]{
                    new C1(0), new C2(0), new C3(0), new C4(0),
                    new C5(0), new C6(0)
            };

            public Object[] getInput() {
                return input;
            }
        }
    }

    public static class Artemis extends OthersBenchmark {

        private final C1 comp = new C1();
        World world;
        Archetype archetype1;
        Archetype archetype2;
        Archetype archetype4;
        Archetype archetype6;
        int[] entities1;
        int[] entities2;
        int[] entities4;
        int[] entities6;
        boolean run1, run2, run4, run6;
        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Trial)
        public void setup() {
            WorldConfiguration worldConfiguration = new WorldConfigurationBuilder().build();
            world = new World(worldConfiguration);

            archetype1 = new ArchetypeBuilder().add(C1.class).build(world);
            archetype2 = new ArchetypeBuilder().add(C1.class).add(C2.class).build(world);
            archetype4 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).build(world);
            archetype6 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).add(C6.class).build(world);

            entities1 = new int[size];
            entities2 = new int[size];
            entities4 = new int[size];
            entities6 = new int[size];

            for (int i = 0; i < size; i++) {
                entities1[i] = world.create(archetype1);
                entities2[i] = world.create(archetype2);
                entities4[i] = world.create(archetype4);
                entities6[i] = world.create(archetype6);
            }
            world.process();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (run1) {
                run1 = false;
                for (int i = 0; i < size; i++) {
                    world.edit(entities1[i]).add(comp);
                }
                world.process();
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    world.edit(entities2[i]).add(comp);
                }
                world.process();
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    world.edit(entities4[i]).add(comp);
                }
                world.process();
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    world.edit(entities6[i]).add(comp);
                }
                world.process();
            }
        }

        @Benchmark
        public void removeComponentFrom01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities1[i]).remove(comp));
            }
            world.process();
            run1 = true;
        }

        @Benchmark
        public void removeComponentFrom02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities2[i]).remove(comp));
            }
            world.process();
            run2 = true;
        }

        @Benchmark
        public void removeComponentFrom04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities4[i]).remove(comp));
            }
            world.process();
            run4 = true;
        }

        @Benchmark
        public void removeComponentFrom06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities6[i]).remove(comp));
            }
            world.process();
            run6 = true;
        }
    }

    public static class C1 extends Component {
    }

    public static class C2 extends Component {
    }

    public static class C3 extends Component {
    }

    public static class C4 extends Component {
    }

    public static class C5 extends Component {
    }

    public static class C6 extends Component {
    }
}
