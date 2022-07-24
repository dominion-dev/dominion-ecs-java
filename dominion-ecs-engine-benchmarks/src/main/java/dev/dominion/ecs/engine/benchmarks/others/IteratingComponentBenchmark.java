/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.others;

import com.artemis.*;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.benchmarks.DominionBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class IteratingComponentBenchmark {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{
                        DominionBenchmark.fetchBenchmarkName(Dominion.class),
                        DominionBenchmark.fetchBenchmarkName(Artemis.class)
                }
        );
    }

    public static class Dominion extends OthersBenchmark {

        EntityRepository entityRepository;
        @Param(value = {"10000000"})
        int size;

        @TearDown(Level.Trial)
        public void tearDown() {
            if (entityRepository == null) return;
            entityRepository.close();
        }

        public static class IterateUnpacking01 extends Dominion {
            @Setup(Level.Trial)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                C1 comp = new C1();
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comp);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp());
                }
            }
        }

        public static class IterateUnpacking02 extends Dominion {
            @Setup(Level.Trial)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                Object[] comps = {new C1(), new C2()};
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comps);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class, C2.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp2());
                }
            }
        }

        public static class IterateUnpacking03 extends Dominion {
            @Setup(Level.Iteration)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                Object[] comps = {new C1(), new C2(), new C3()};
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comps);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp3());
                }
            }
        }

        public static class IterateUnpacking04 extends Dominion {
            @Setup(Level.Iteration)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                Object[] comps = {new C1(), new C2(), new C3(), new C4()};
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comps);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp4());
                }
            }
        }

        public static class IterateUnpacking05 extends Dominion {
            @Setup(Level.Iteration)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                Object[] comps = {new C1(), new C2(), new C3(), new C4(), new C5()};
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comps);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp5());
                }
            }
        }

        public static class IterateUnpacking06 extends Dominion {
            @Setup(Level.Iteration)
            public void setup() {
                entityRepository = (EntityRepository) new EntityRepository.Factory().create();
                Object[] comps = {new C1(), new C2(), new C3(), new C4(), new C5(), new C6()};
                for (int i = 0; i < size; i++) {
                    entityRepository.createEntity(comps);
                }
            }

            @Benchmark
            public void iterate(Blackhole bh) {
                var iterator = entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator();
                while (iterator.hasNext()) {
                    bh.consume(iterator.next().comp6());
                }
            }
        }
    }


    public static class Artemis extends OthersBenchmark {
        World world;
        Archetype archetype;
        int[] entities;

        @Param(value = {"10000000"})
        int size;

        public static class IterateUnpacking01 extends Artemis {
            @Setup(Level.Iteration)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking01();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        public static class IterateUnpacking02 extends Artemis {
            @Setup(Level.Iteration)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking02();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).add(C2.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        public static class IterateUnpacking03 extends Artemis {
            @Setup(Level.Iteration)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking03();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        public static class IterateUnpacking04 extends Artemis {
            @Setup(Level.Iteration)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking04();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        public static class IterateUnpacking05 extends Artemis {
            @Setup(Level.Iteration)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking05();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        public static class IterateUnpacking06 extends Artemis {
            @Setup(Level.Trial)
            public void setup(Blackhole bh) {
                var system = new SystemUnpacking06();
                system.blackhole = bh;
                WorldConfiguration worldConfiguration = new WorldConfigurationBuilder()
                        .with(system)
                        .build();
                world = new World(worldConfiguration);
                archetype = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).add(C6.class).build(world);
                entities = new int[size];
                for (int i = 0; i < size; i++) {
                    entities[i] = world.create(archetype);
                }
                world.process();
            }

            @Benchmark
            public void iterate() {
                world.process();
            }
        }

        @All(C1.class)
        public static class SystemUnpacking01 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                blackhole.consume(c1Mapper.get(i));
            }
        }

        @All({C1.class, C2.class})
        public static class SystemUnpacking02 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            ComponentMapper<C2> c2Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                c1Mapper.get(i);
                blackhole.consume(c2Mapper.get(i));
            }
        }

        @All({C1.class, C2.class, C3.class})
        public static class SystemUnpacking03 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            ComponentMapper<C2> c2Mapper;
            ComponentMapper<C3> c3Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                c1Mapper.get(i);
                c2Mapper.get(i);
                blackhole.consume(c3Mapper.get(i));
            }
        }

        @All({C1.class, C2.class, C3.class, C4.class})
        public static class SystemUnpacking04 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            ComponentMapper<C2> c2Mapper;
            ComponentMapper<C3> c3Mapper;
            ComponentMapper<C4> c4Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                c1Mapper.get(i);
                c2Mapper.get(i);
                c3Mapper.get(i);
                blackhole.consume(c4Mapper.get(i));
            }
        }


        @All({C1.class, C2.class, C3.class, C4.class, C5.class})
        public static class SystemUnpacking05 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            ComponentMapper<C2> c2Mapper;
            ComponentMapper<C3> c3Mapper;
            ComponentMapper<C4> c4Mapper;
            ComponentMapper<C5> c5Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                c1Mapper.get(i);
                c2Mapper.get(i);
                c3Mapper.get(i);
                c4Mapper.get(i);
                blackhole.consume(c5Mapper.get(i));
            }
        }

        @All({C1.class, C2.class, C3.class, C4.class, C5.class, C6.class})
        public static class SystemUnpacking06 extends IteratingSystem {
            ComponentMapper<C1> c1Mapper;
            ComponentMapper<C2> c2Mapper;
            ComponentMapper<C3> c3Mapper;
            ComponentMapper<C4> c4Mapper;
            ComponentMapper<C5> c5Mapper;
            ComponentMapper<C6> c6Mapper;
            Blackhole blackhole;

            @Override
            protected void process(int i) {
                c1Mapper.get(i);
                c2Mapper.get(i);
                c3Mapper.get(i);
                c4Mapper.get(i);
                c5Mapper.get(i);
                blackhole.consume(c6Mapper.get(i));
            }
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
