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

public class RemovingComponentBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(RemovingComponentBenchmark.Dominion.class),
                        DominionBenchmark.fetchBenchmarkName(RemovingComponentBenchmark.Artemis.class)
                }
        );
    }

    public static class Dominion extends OthersBenchmark {
        EntityRepository entityRepository;
        Entity[] entities1;
        Entity[] entities2;
        Entity[] entities4;
        Entity[] entities6;

        boolean run1, run2, run4, run6;

        Object input1 = new C1();
        Object[] input2 = {new C1(), new C2()};
        Object[] input4 = {new C1(), new C2(), new C3(), new C4()};
        Object[] input6 = {new C1(), new C2(), new C3(), new C4(), new C5(), new C6()};

        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Trial)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities1 = new Entity[size];
            entities2 = new Entity[size];
            entities4 = new Entity[size];
            entities6 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities1[i] = entityRepository.createEntity(input1);
                entities2[i] = entityRepository.createEntity(input2);
                entities4[i] = entityRepository.createEntity(input4);
                entities6[i] = entityRepository.createEntity(input6);
            }
            run1 = run2 = run4 = run6 = false;
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (run1) {
                run1 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities1[i]);
                    entities1[i] = entityRepository.createEntity(input1);
                }
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities2[i]);
                    entities2[i] = entityRepository.createEntity(input2);
                }
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities4[i]);
                    entities4[i] = entityRepository.createEntity(input4);
                }
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities6[i]);
                    entities6[i] = entityRepository.createEntity(input6);
                }
            }
        }

        @Benchmark
        public void removeComponentFrom01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities1[i].removeType(C1.class));
            }
            run1 = true;
        }

        @Benchmark
        public void removeComponentFrom02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities2[i].removeType(C1.class));
            }
            run2 = true;
        }

        @Benchmark
        public void removeComponentFrom04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities4[i].removeType(C1.class));
            }
            run4 = true;
        }

        @Benchmark
        public void removeComponentFrom06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities6[i].removeType(C1.class));
            }
            run6 = true;
        }
    }

    public static class Artemis extends OthersBenchmark {

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

        @Setup(Level.Iteration)
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
                    world.delete(entities1[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities1[i] = world.create(archetype1);
                }
                world.process();
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities2[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities2[i] = world.create(archetype2);
                }
                world.process();
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities4[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities4[i] = world.create(archetype4);
                }
                world.process();
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    world.delete(entities6[i]);
                }
                world.process();
                for (int i = 0; i < size; i++) {
                    entities6[i] = world.create(archetype6);
                }
                world.process();
            }
        }

        @Benchmark
        public void removeComponentFrom01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities1[i]).remove(new C1()));
            }
            world.process();
            run1 = true;
        }

        @Benchmark
        public void removeComponentFrom02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities2[i]).remove(new C1()));
            }
            world.process();
            run2 = true;
        }

        @Benchmark
        public void removeComponentFrom04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities4[i]).remove(new C1()));
            }
            world.process();
            run4 = true;
        }

        @Benchmark
        public void removeComponentFrom06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(world.edit(entities6[i]).remove(new C1()));
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
