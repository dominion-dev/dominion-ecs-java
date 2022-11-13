/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.others;

import com.artemis.*;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class AddingComponentBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(AddingComponentBenchmark.Dominion.class),
                        DominionBenchmark.fetchBenchmarkName(AddingComponentBenchmark.Artemis.class)
                }
        );
    }

    public static class Dominion extends OthersBenchmark {
        EntityRepository entityRepository;
        Entity[] entities0;
        Entity[] entities1;
        Entity[] entities3;
        Entity[] entities5;

        boolean run1, run2, run4, run6;

        Object[] input1 = {new C1()};
        Object[] input3 = {new C1(), new C2(), new C3()};
        Object[] input5 = {new C1(), new C2(), new C3(), new C4(), new C5()};

        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Trial)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities0 = new Entity[size];
            entities1 = new Entity[size];
            entities3 = new Entity[size];
            entities5 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities0[i] = entityRepository.createEntity();
                entities1[i] = entityRepository.createEntity(input1);
                entities3[i] = entityRepository.createEntity(input3);
                entities5[i] = entityRepository.createEntity(input5);
            }
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (run1) {
                run1 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities0[i]);
                    entities0[i] = entityRepository.createEntity();
                }
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities1[i]);
                    entities1[i] = entityRepository.createEntity(input1);
                }
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities3[i]);
                    entities3[i] = entityRepository.createEntity(input3);
                }
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities5[i]);
                    entities5[i] = entityRepository.createEntity(input5);
                }
            }
        }

        @Benchmark
        public void addComponentUpTo01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities0[i].add(new C0()));
            }
            run1 = true;
        }

        @Benchmark
        public void addComponentUpTo02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities1[i].add(new C0()));
            }
            run2 = true;
        }

        @Benchmark
        public void addComponentUpTo04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities3[i].add(new C0()));
            }
            run4 = true;
        }

        @Benchmark
        public void addComponentUpTo06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities5[i].add(new C0()));
            }
            run6 = true;
        }
    }

    public static class Artemis extends OthersBenchmark {

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

        @Setup(Level.Iteration)
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
                for (int i = 0; i < size; i++) {
                    world.delete(entities0[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities0[i] = world.create(archetype0);
                }
                world.process();
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities1[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities1[i] = world.create(archetype1);
                }
                world.process();
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities3[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities3[i] = world.create(archetype3);
                }
                world.process();
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities5[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities5[i] = world.create(archetype5);
                }
                world.process();
            }
        }

        @Benchmark
        public void addComponentUpTo01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities0[i]).add(new C0()));
            }
            world.process();
            run1 = true;
        }

        @Benchmark
        public void addComponentUpTo02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities1[i]).add(new C0()));
            }
            world.process();
            run2 = true;
        }

        @Benchmark
        public void addComponentUpTo04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities3[i]).add(new C0()));
            }
            world.process();
            run4 = true;
        }

        @Benchmark
        public void addComponentUpTo06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities5[i]).add(new C0()));
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
