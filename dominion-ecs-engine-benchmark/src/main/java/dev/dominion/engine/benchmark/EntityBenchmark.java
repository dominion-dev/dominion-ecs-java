/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.engine.benchmark;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.IntEntity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

public class EntityBenchmark extends DominionBenchmark {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(EntityBenchmark.class)}
        );
    }

    enum State1 {
        ONE
    }

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

    record C6(int id) {
    }

    record C7(int id) {
    }

    record C8(int id) {
    }

    record C9(int id) {
    }

    record C10(int id) {
    }

    record C11(int id) {
    }

    record C12(int id) {
    }

    record C13(int id) {
    }

    record C14(int id) {
    }

    record C15(int id) {
    }

    record C16(int id) {
    }

    public static class EntityLayout {
        public static void main(String[] args) {
            System.out.println(VM.current().details());
            System.out.println(ClassLayout.parseClass(IntEntity.class).toPrintable());
            System.out.println(ClassLayout.parseInstance(new IntEntity(0, null)).toPrintable());
        }
    }


    public static class EntityMethodBenchmark extends DominionBenchmark {
        EntityRepository entityRepository;
        Entity[] entities;

        @Param(value = {"1000000"})
        int size;

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities[i] = entityRepository.createEntity();
            }
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < size; i++) {
                entityRepository.deleteEntity(entities[i]);
                entities[i] = entityRepository.createEntity(getInput());
            }
        }

        public Object[] getInput() {
            return null;
        }


        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    // Add

    public static class Add extends EntityMethodBenchmark {
        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add.class)}
            );
        }
    }

    public static class Add01 extends Add {
        Object[] input = new Object[]{new C1(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add01.class)}
            );
        }

        @Benchmark
        public void add(Blackhole bh) {
            var c0 = new C0(0);
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i].add(c0));
            }
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Add02 extends Add01 {
        Object[] input = new Object[]{new C1(0), new C2(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add02.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Add04 extends Add01 {
        Object[] input = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add04.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Add08 extends Add01 {
        Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0)
        };

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add08.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Add16 extends Add01 {
        Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Add16.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    // Remove

    public static class Remove extends EntityMethodBenchmark {
        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove.class)}
            );
        }
    }

    public static class Remove01 extends Remove {
        Object[] input = new Object[]{new C2(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove01.class)}
            );
        }

        @Benchmark
        public void remove(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i].removeType(C2.class));
            }
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Remove02 extends Remove01 {
        Object[] input = new Object[]{new C1(0), new C2(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove02.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Remove04 extends Remove01 {
        Object[] input = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove04.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Remove08 extends Remove01 {
        Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0)
        };

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove08.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }

    public static class Remove16 extends Remove01 {
        Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Remove16.class)}
            );
        }

        public Object[] getInput() {
            return input;
        }
    }


    // Others

    public static class Has extends DominionBenchmark {
        EntityRepository entityRepository;
        Entity[] entities01;
        Entity[] entities04;
        Entity[] entities08;
        Entity[] entities16;
        Object input01 = new C1(0);
        Object[] input04 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
        };
        Object[] input08 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0)
        };
        Object[] input16 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Has.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities01 = new Entity[size];
            entities04 = new Entity[size];
            entities08 = new Entity[size];
            entities16 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities01[i] = entityRepository.createEntity(input01);
                entities04[i] = entityRepository.createEntity(input04);
                entities08[i] = entityRepository.createEntity(input08);
                entities16[i] = entityRepository.createEntity(input16);
            }
        }

        @Benchmark
        public void has01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities01[i].has(C1.class));
            }
        }

        @Benchmark
        public void has04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities04[i].has(C1.class));
            }
        }

        @Benchmark
        public void has08(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities08[i].has(C1.class));
            }
        }

        @Benchmark
        public void has16(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities16[i].has(C1.class));
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    public static class SetEnabled extends DominionBenchmark {
        EntityRepository entityRepository;
        Entity[] entities16;
        Object[] input16 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(SetEnabled.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities16 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities16[i] = entityRepository.createEntity(input16);
            }
        }

        @Benchmark
        public void setEnabled(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                entities16[i].setEnabled(false);
                bh.consume(entities16[i].isEnabled());
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    public static class SetState extends DominionBenchmark {
        EntityRepository entityRepository;
        Entity[] entities01;
        Entity[] entities04;
        Entity[] entities08;
        Entity[] entities16;
        Object input01 = new C1(0);
        Object[] input04 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
        };
        Object[] input08 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0)
        };
        Object[] input16 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(SetState.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities01 = new Entity[size];
            entities04 = new Entity[size];
            entities08 = new Entity[size];
            entities16 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities01[i] = entityRepository.createEntity(input01);
                entities04[i] = entityRepository.createEntity(input04);
                entities08[i] = entityRepository.createEntity(input08);
                entities16[i] = entityRepository.createEntity(input16);
            }
        }

        @Benchmark
        public void setStateTo01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities01[i].setState(State1.ONE));
            }
        }

        @Benchmark
        public void setStateTo04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities04[i].setState(State1.ONE));
            }
        }

        @Benchmark
        public void setStateTo08(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities08[i].setState(State1.ONE));
            }
        }

        @Benchmark
        public void setStateTo16(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities16[i].setState(State1.ONE));
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    public static class Contains extends DominionBenchmark {
        EntityRepository entityRepository;
        Entity[] entities01;
        Entity[] entities04;
        Entity[] entities08;
        Entity[] entities16;
        Object input01 = new C1(0);
        Object[] input04 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
        };
        Object[] input08 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0)
        };
        Object[] input16 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0),
                new C5(0), new C6(0), new C7(0), new C8(0),
                new C9(0), new C10(0), new C11(0), new C12(0),
                new C13(0), new C14(0), new C15(0), new C16(0)
        };

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(Contains.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities01 = new Entity[size];
            entities04 = new Entity[size];
            entities08 = new Entity[size];
            entities16 = new Entity[size];
            for (int i = 0; i < size; i++) {
                entities01[i] = entityRepository.createEntity(input01);
                entities04[i] = entityRepository.createEntity(input04);
                entities08[i] = entityRepository.createEntity(input08);
                entities16[i] = entityRepository.createEntity(input16);
            }
        }

        @Benchmark
        public void contains01(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities01[i].contains(input01));
            }
        }

        @Benchmark
        public void contains04(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities04[i].contains(input01));
            }
        }

        @Benchmark
        public void contains08(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities08[i].contains(input01));
            }
        }

        @Benchmark
        public void contains16(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities16[i].contains(input16[0]));
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }
}
