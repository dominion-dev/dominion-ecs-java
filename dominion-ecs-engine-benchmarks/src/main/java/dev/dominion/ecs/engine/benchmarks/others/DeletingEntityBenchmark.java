/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.others;

import com.artemis.*;
import dev.dominion.ecs.api.Composition;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class DeletingEntityBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(DeletingEntityBenchmark.Dominion.class),
                        DominionBenchmark.fetchBenchmarkName(DeletingEntityBenchmark.Artemis.class)
                }
        );
    }

    public static class Dominion extends OthersBenchmark {
        EntityRepository entityRepository;
        Composition.Of1<C1> composition1;
        Composition.Of2<C1, C2> composition2;
        Composition.Of4<C1, C2, C3, C4> composition4;
        Composition.Of6<C1, C2, C3, C4, C5, C6> composition6;
        Entity[] entities1;
        Entity[] entities2;
        Entity[] entities4;
        Entity[] entities6;

        boolean run1, run2, run4, run6;

        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Trial)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Composition composition = entityRepository.composition();
            composition1 = composition.of(C1.class);
            composition2 = composition.of(C1.class, C2.class);
            composition4 = composition.of(C1.class, C2.class, C3.class, C4.class);
            composition6 = composition.of(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class);
            entities1 = new Entity[size];
            entities2 = new Entity[size];
            entities4 = new Entity[size];
            entities6 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities1[i] = entityRepository.createPreparedEntity(composition1.withValue(new C1()));
                entities2[i] = entityRepository.createPreparedEntity(composition2.withValue(new C1(), new C2()));
                entities4[i] = entityRepository.createPreparedEntity(composition4.withValue(new C1(), new C2(), new C3(), new C4()));
                entities6[i] = entityRepository.createPreparedEntity(composition6.withValue(new C1(), new C2(), new C3(), new C4(), new C5(), new C6()));
            }
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (run1) {
                run1 = false;
                for (int i = 0; i < size; i++) {
                    entities1[i] = entityRepository.createPreparedEntity(composition1.withValue(new C1()));
                }
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    entities2[i] = entityRepository.createPreparedEntity(composition2.withValue(new C1(), new C2()));
                }
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    entities4[i] = entityRepository.createPreparedEntity(composition4.withValue(new C1(), new C2(), new C3(), new C4()));
                }
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    entities6[i] = entityRepository.createPreparedEntity(composition6.withValue(new C1(), new C2(), new C3(), new C4(), new C5(), new C6()));
                }
            }
        }

        @Benchmark
        public void deleteEntityWith01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities1[i]));
            }
            run1 = true;
        }

        @Benchmark
        public void deleteEntityWith02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities2[i]));
            }
            run2 = true;
        }

        @Benchmark
        public void deleteEntityWith04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities4[i]));
            }
            run4 = true;
        }

        @Benchmark
        public void deleteEntityWith06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities6[i]));
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
        @Param(value = {"1000000"})
        int size;
        boolean run1, run2, run4, run6;

        public static void main(String[] args) throws IOException {
            org.openjdk.jmh.Main.main(
                    new String[]{DeletingEntityBenchmark.class.getName()}
            );
        }

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
                    entities1[i] = world.create(archetype1);
                }
                world.process();
            }

            if (run2) {
                run2 = false;
                for (int i = 0; i < size; i++) {
                    entities2[i] = world.create(archetype2);
                }
                world.process();
            }

            if (run4) {
                run4 = false;
                for (int i = 0; i < size; i++) {
                    entities4[i] = world.create(archetype4);
                }
                world.process();
            }

            if (run6) {
                run6 = false;
                for (int i = 0; i < size; i++) {
                    entities6[i] = world.create(archetype6);
                }
                world.process();
            }
        }

        @Benchmark
        public void deleteEntityWith01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(delete(entities1[i]));
            }
            world.process();
            run1 = true;
        }

        @Benchmark
        public void deleteEntityWith02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(delete(entities2[i]));
            }
            world.process();
            run2 = true;
        }

        @Benchmark
        public void deleteEntityWith04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(delete(entities4[i]));
            }
            world.process();
            run4 = true;
        }

        @Benchmark
        public void deleteEntityWith06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(delete(entities6[i]));
            }
            world.process();
            run6 = true;
        }

        private int delete(int entity) {
            world.delete(entity);
            return entity;
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
