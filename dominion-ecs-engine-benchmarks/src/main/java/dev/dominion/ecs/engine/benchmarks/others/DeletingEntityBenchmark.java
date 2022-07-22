/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks.others;

import com.artemis.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

public class DeletingEntityBenchmark extends OthersBenchmark {

    World world;
    Archetype archetype1;
    Archetype archetype2;
    Archetype archetype4;
    Archetype archetype8;


    int[] entities1;
    int[] entities2;
    int[] entities4;
    int[] entities8;
    @Param(value = {"1000000"})
    int size;

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{DeletingEntityBenchmark.class.getName()}
        );
    }

    @Setup(Level.Iteration)
    public void setup() {
        WorldConfiguration worldConfiguration = new WorldConfigurationBuilder().build();
        world = new World(worldConfiguration);
        archetype1 = new ArchetypeBuilder().add(C1.class).build(world);
        archetype2 = new ArchetypeBuilder().add(C1.class).add(C2.class).build(world);
        archetype4 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).build(world);
        archetype8 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).add(C6.class).add(C7.class).add(C8.class).build(world);
        entities1 = new int[size];
        entities2 = new int[size];
        entities4 = new int[size];
        entities8 = new int[size];
        world.process();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        for (int i = 0; i < size; i++) {
            world.delete(entities1[i]);
            world.delete(entities2[i]);
            world.delete(entities4[i]);
            world.delete(entities8[i]);
        }
        for (int i = 0; i < size; i++) {
            entities1[i] = world.create(archetype1);
            entities2[i] = world.create(archetype2);
            entities4[i] = world.create(archetype4);
            entities8[i] = world.create(archetype8);
        }
        world.process();
    }

    @Benchmark
    public void deleteEntityWith01(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(delete(entities1[i]));
        }
        world.process();
    }

    @Benchmark
    public void deleteEntityWith02(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(delete(entities2[i]));
        }
        world.process();
    }

    @Benchmark
    public void deleteEntityWith04(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(delete(entities4[i]));
        }
        world.process();
    }

    @Benchmark
    public void deleteEntityWith08(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(delete(entities8[i]));
        }
        world.process();
    }

    private int delete(int entity) {
        world.delete(entity);
        return entity;
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

    public static class C7 extends Component {
    }

    public static class C8 extends Component {
    }
}
