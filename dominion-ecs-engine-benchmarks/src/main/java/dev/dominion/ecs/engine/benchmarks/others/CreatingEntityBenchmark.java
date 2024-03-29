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

public class CreatingEntityBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(Dominion.class),
                        DominionBenchmark.fetchBenchmarkName(Artemis.class)
                }
        );
    }

    public static class Dominion extends OthersBenchmark {
        // DOMINION
        EntityRepository entityRepository;
        Composition.Of1<C1> composition1;
        Composition.Of2<C1, C2> composition2;
        Composition.Of4<C1, C2, C3, C4> composition4;
        Composition.Of6<C1, C2, C3, C4, C5, C6> composition6;
        Entity[] entities;

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
            entities = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities[i] = entityRepository.createEntity();
            }
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < size; i++) {
                entityRepository.deleteEntity(entities[i]);
            }
        }

        @Benchmark
        public void createEntityWith01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createPreparedEntity(composition1.withValue(new C1())));
            }
        }

        @Benchmark
        public void createEntityWith02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createPreparedEntity(composition2.withValue(new C1(), new C2())));
            }
        }

        @Benchmark
        public void createEntityWith04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createPreparedEntity(composition4.withValue(new C1(), new C2(), new C3(), new C4())));
            }
        }

        @Benchmark
        public void createEntityWith06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createPreparedEntity(composition6.withValue(new C1(), new C2(), new C3(), new C4(), new C5(), new C6())));
            }
        }
    }

    public static class Artemis extends OthersBenchmark {
        World world;
        Archetype archetype1;
        Archetype archetype2;
        Archetype archetype4;
        Archetype archetype6;
        int[] entities;
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
            entities = new int[size];
            for (int i = 0; i < size; i++) {
                entities[i] = world.create(archetype1);
            }
            world.process();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < size; i++) {
                world.delete(entities[i]);
            }
            world.process();
        }

        @Benchmark
        public void createEntityWith01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = world.create(archetype1));
            }
            world.process();
        }

        @Benchmark
        public void createEntityWith02(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = world.create(archetype2));
            }
            world.process();
        }

        @Benchmark
        public void createEntityWith04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = world.create(archetype4));
            }
            world.process();
        }

        @Benchmark
        public void createEntityWith06(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = world.create(archetype6));
            }
            world.process();
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
