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

public class AddingComponentBenchmark extends OthersBenchmark {

    World world;
    Archetype archetype0;
    Archetype archetype1;
    Archetype archetype3;
    Archetype archetype7;


    int[] entities0;
    int[] entities1;
    int[] entities3;

    int[] entities7;

    @Param(value = {"1000000"})
    int size;

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(
                new String[]{AddingComponentBenchmark.class.getName()}
        );
    }

    @Setup(Level.Iteration)
    public void setup() {
        WorldConfiguration worldConfiguration = new WorldConfigurationBuilder().build();
        world = new World(worldConfiguration);
        archetype0 = new ArchetypeBuilder().build(world);
        archetype1 = new ArchetypeBuilder().add(C1.class).build(world);
        archetype3 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).build(world);
        archetype7 = new ArchetypeBuilder().add(C1.class).add(C2.class).add(C3.class).add(C4.class).add(C5.class).add(C6.class).add(C7.class).build(world);
        entities0 = new int[size];
        entities1 = new int[size];
        entities3 = new int[size];
        entities7 = new int[size];
        for (int i = 0; i < size; i++) {
            entities0[i] = world.create(archetype0);
            entities1[i] = world.create(archetype1);
            entities3[i] = world.create(archetype3);
            entities7[i] = world.create(archetype7);
        }
        world.process();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        for (int i = 0; i < size; i++) {
            world.delete(entities0[i]);
            world.delete(entities1[i]);
            world.delete(entities3[i]);
            world.delete(entities7[i]);
        }
        world.process();
        for (int i = 0; i < size; i++) {
            entities0[i] = world.create(archetype0);
            entities1[i] = world.create(archetype1);
            entities3[i] = world.create(archetype3);
            entities7[i] = world.create(archetype7);
        }
        world.process();
    }

    @Benchmark
    public void addComponentUpTo01(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(world.edit(entities0[i]).add(new C0()));
        }
        world.process();
    }

    @Benchmark
    public void addComponentUpTo02(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(world.edit(entities1[i]).add(new C0()));
        }
        world.process();
    }

    @Benchmark
    public void addComponentUpTo04(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(world.edit(entities3[i]).add(new C0()));
        }
        world.process();
    }

    @Benchmark
    public void addComponentUpTo08(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(world.edit(entities7[i]).add(new C0()));
        }
        world.process();
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

    public static class C6 extends Component {
    }

    public static class C7 extends Component {
    }
}
