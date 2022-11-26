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

public class AddingComponentBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(AddingComponentBenchmark.Dominion.class),
//                        DominionBenchmark.fetchBenchmarkName(AddingComponentBenchmark.Artemis.class)
                }
        );
    }

    public static class Dominion {

        record C0(int id) {
        }

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

        @BenchmarkMode(Mode.Throughput)
        public static class AddUpTo01 extends EntityBenchmark.EntityMethodBenchmark {
            Object[] input = new Object[]{};
            C0 c0 = new C0(0);

            @Benchmark
            public void add(Blackhole bh) {
                for (int i = 0; i < size; i++) {
                    bh.consume(entities[i].add(c0));
                }
            }

            public Object[] getInput() {
                return input;
            }
        }

        public static class AddUpTo02 extends AddUpTo01 {
            Object[] input = new Object[]{new C1(0)};

            public Object[] getInput() {
                return input;
            }
        }

        public static class AddUpTo04 extends AddUpTo01 {
            Object[] input = new Object[]{new C1(0), new C2(0), new C3(0)};

            public Object[] getInput() {
                return input;
            }
        }

        public static class AddUpTo06 extends AddUpTo01 {
            Object[] input = new Object[]{
                    new C1(0), new C2(0), new C3(0), new C4(0),
                    new C5(0)
            };

            public Object[] getInput() {
                return input;
            }
        }
    }

    public static class Artemis extends OthersBenchmark {

        private final C0 comp = new C0();
        World world;
        Archetype archetype0;
        Archetype archetype1;
        Archetype archetype3;
        Archetype archetype5;
        int[] entities0;
        int[] entities1;
        int[] entities3;
        int[] entities5;
        boolean run1, run2, run4, run6;
        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Trial)
        public void setup() {
            WorldConfiguration worldConfiguration = new WorldConfigurationBuilder().build();
            world = new World(worldConfiguration);

            archetype0 = new ArchetypeBuilder().build(world);
            archetype1 = new ArchetypeBuilder().add(C1.class).build(world);
            archetype3 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).build(world);
            archetype5 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).build(world);

            entities0 = new int[size];
            entities1 = new int[size];
            entities3 = new int[size];
            entities5 = new int[size];

            for (int i = 0; i < size; i++) {
                entities0[i] = world.create(archetype0);
                entities1[i] = world.create(archetype1);
                entities3[i] = world.create(archetype3);
                entities5[i] = world.create(archetype5);
            }
            world.process();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (run1) {
                run1 = false;
                for (int i = 0; i < size; i++) world.delete(entities0[i]);
                world.process();
                for (int i = 0; i < size; i++) entities0[i] = world.create(archetype0);
                world.process();
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) world.delete(entities1[i]);
                world.process();
                for (int i = 0; i < size; i++) entities1[i] = world.create(archetype1);
                world.process();
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) world.delete(entities3[i]);
                world.process();
                for (int i = 0; i < size; i++) entities3[i] = world.create(archetype3);
                world.process();
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) world.delete(entities5[i]);
                world.process();
                for (int i = 0; i < size; i++) entities5[i] = world.create(archetype5);
                world.process();
            }
        }

        @Benchmark
        public void addComponentUpTo01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities0[i]).add(comp));
            }
            world.process();
            run1 = true;
        }

        @Benchmark
        public void addComponentUpTo02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities1[i]).add(comp));
            }
            world.process();
            run2 = true;
        }

        @Benchmark
        public void addComponentUpTo04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities3[i]).add(comp));
            }
            world.process();
            run4 = true;
        }

        @Benchmark
        public void addComponentUpTo06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities5[i]).add(comp));
            }
            world.process();
            run6 = true;
        }
    }

    public static class C0 extends Component {
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
}
