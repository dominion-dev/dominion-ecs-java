/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.benchmarks;

import dev.dominion.ecs.api.Composition;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.EntityRepository;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

public class EntityRepositoryBenchmark extends DominionBenchmark {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(
                new String[]{fetchBenchmarkName(EntityRepositoryBenchmark.class)}
        );
    }

    enum State1 {
        ONE
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

    public static class CreateEntity extends DominionBenchmark {
        Object[] input = new Object[0];

        EntityRepository entityRepository;
        Entity[] entities;

        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntity.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            onSetup();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (entities == null) {
                entities = new Entity[size];
            } else {
                for (int i = 0; i < size; i++) {
                    entityRepository.deleteEntity(entities[i]);
                }
            }
        }

        @Benchmark
        public void createEntity(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createEntity(getInput()));
            }
        }

        @Benchmark
        public void createPreparedEntity(Blackhole bh) {
            if (getPreparedInput() == null) {
                return;
            }
            for (int i = 0; i < size; i++) {
                bh.consume(entities[i] = entityRepository.createPreparedEntity(getPreparedInput()));
            }
        }

        public void onSetup() {
        }

        public Object[] getInput() {
            return input;
        }

        public Composition.OfTypes getPreparedInput() {
            return null;
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    public static class CreateEntityWith01Component extends CreateEntity {
        Object[] input = new Object[]{new C1(0)};

        Composition.Of1<C1> composition;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntityWith01Component.class)}
            );
        }

        @Override
        public void onSetup() {
            composition = entityRepository.composition().of(C1.class);
        }

        @Override
        public Object[] getInput() {
            return input;
        }

        @Override
        public Composition.OfTypes getPreparedInput() {
            return composition.withValue(new C1(0));
        }
    }

    public static class CreateEntityWith02Component extends CreateEntity {
        Object[] input = new Object[]{new C1(0), new C2(0)};

        Composition.Of2<C1, C2> composition;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntityWith02Component.class)}
            );
        }

        @Override
        public void onSetup() {
            composition = entityRepository.composition().of(C1.class, C2.class);
        }

        public Object[] getInput() {
            return input;
        }

        @Override
        public Composition.OfTypes getPreparedInput() {
            return composition.withValue(new C1(0), new C2(0));
        }
    }

    public static class CreateEntityWith04Component extends CreateEntity {
        Object[] input = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};

        Composition.Of4<C1, C2, C3, C4> composition;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntityWith04Component.class)}
            );
        }

        @Override
        public void onSetup() {
            composition = entityRepository.composition().of(C1.class, C2.class, C3.class, C4.class);
        }

        public Object[] getInput() {
            return input;
        }

        @Override
        public Composition.OfTypes getPreparedInput() {
            return composition.withValue(new C1(0), new C2(0), new C3(0), new C4(0));
        }
    }

    public static class CreateEntityWith06Component extends CreateEntity {
        private final Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
                , new C5(0), new C6(0)
        };

        Composition.Of6<C1, C2, C3, C4, C5, C6> composition;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntityWith06Component.class)}
            );
        }

        @Override
        public void onSetup() {
            composition = entityRepository.composition().of(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class);
        }

        @Override
        public Object[] getInput() {
            return input;
        }

        @Override
        public Composition.OfTypes getPreparedInput() {
            return composition.withValue(new C1(0), new C2(0), new C3(0), new C4(0), new C5(0), new C6(0));
        }
    }

    public static class CreateEntityWith08Component extends CreateEntity {
        private final Object[] input = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
                , new C5(0), new C6(0), new C7(0), new C8(0)
        };

        Composition.Of8<C1, C2, C3, C4, C5, C6, C7, C8> composition;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(CreateEntityWith08Component.class)}
            );
        }

        @Override
        public void onSetup() {
            composition = entityRepository.composition().of(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class, C7.class, C8.class);
        }

        @Override
        public Object[] getInput() {
            return input;
        }

        @Override
        public Composition.OfTypes getPreparedInput() {
            return composition.withValue(new C1(0), new C2(0), new C3(0), new C4(0), new C5(0), new C6(0), new C7(0), new C8(0));
        }
    }


    public static class DeleteEntity extends DominionBenchmark {
        private final Object[] input1 = new Object[]{
                new C1(0)
        };
        private final Object[] input2 = new Object[]{
                new C1(0), new C2(0)
        };
        private final Object[] input4 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
        };
        private final Object[] input6 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
                , new C5(0), new C6(0)
        };
        private final Object[] input8 = new Object[]{
                new C1(0), new C2(0), new C3(0), new C4(0)
                , new C5(0), new C6(0), new C7(0), new C8(0)
        };

        EntityRepository entityRepository;
        Entity[] entities1;
        Entity[] entities2;
        Entity[] entities4;
        Entity[] entities6;
        Entity[] entities8;
        @Param(value = {"1000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(DeleteEntity.class)}
            );
        }

        @Setup(Level.Iteration)
        public void setup() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            entities1 = new Entity[size];
            entities2 = new Entity[size];
            entities4 = new Entity[size];
            entities6 = new Entity[size];
            entities8 = new Entity[size];
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            if (entities1[0] == null || entities1[0].isDeleted())
                for (int i = 0; i < size; i++) entities1[i] = entityRepository.createEntity(input1);
            if (entities2[0] == null || entities2[0].isDeleted())
                for (int i = 0; i < size; i++) entities2[i] = entityRepository.createEntity(input2);
            if (entities4[0] == null || entities4[0].isDeleted())
                for (int i = 0; i < size; i++) entities4[i] = entityRepository.createEntity(input4);
            if (entities6[0] == null || entities6[0].isDeleted())
                for (int i = 0; i < size; i++) entities6[i] = entityRepository.createEntity(input6);
            if (entities8[0] == null || entities8[0].isDeleted())
                for (int i = 0; i < size; i++) entities8[i] = entityRepository.createEntity(input8);

        }

        @Benchmark
        public void deleteEntityOf1(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities1[i]));
            }
        }

        @Benchmark
        public void deleteEntityOf2(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities2[i]));
            }
        }

        @Benchmark
        public void deleteEntityOf4(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities4[i]));
            }
        }

        @Benchmark
        public void deleteEntityOf6(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities6[i]));
            }
        }

        @Benchmark
        public void deleteEntityOf8(Blackhole bh) {
            for (int i = 0; i < size; i++) {
                bh.consume(entityRepository.deleteEntity(entities8[i]));
            }
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            entityRepository.close();
        }
    }

    // FIND Components

    public static abstract class FindComponents extends DominionBenchmark {
        EntityRepository entityRepository;
        @Param(value = {"10000000"})
        int size;

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents.class)}
            );
        }

        @Setup()
        public void setup() {
            setupImpl();
        }

        abstract void setupImpl();

        @Benchmark
        public void iterate(Blackhole bh) {
            iterateImpl(bh);
        }

        @Benchmark
        public void iterateWithState(Blackhole bh) {
            iterateWithStateImpl(bh);
        }

        @Benchmark
        public void stream(Blackhole bh) {
            streamImpl(bh);
        }

        @Benchmark
        public void streamWithState(Blackhole bh) {
            streamWithStateImpl(bh);
        }

        abstract void iterateImpl(Blackhole bh);

        abstract void iterateWithStateImpl(Blackhole bh);

        abstract void streamImpl(Blackhole bh);

        abstract void streamWithStateImpl(Blackhole bh);

        @TearDown()
        public void tearDown() {
            entityRepository.close();
        }
    }

    // 1

    public static class FindComponents1 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents1.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            C1 comp = new C1(0);
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comp).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator = entityRepository.findCompositionsWith(C1.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator = entityRepository.findEntitiesWith(C1.class).withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream = entityRepository.findCompositionsWith(C1.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findEntitiesWith(C1.class).withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents1FromMoreCompositions extends FindComponents1 {
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents1FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            Object[] comps2 = new Object[]{new C1(0), new C2(0)};
//            C1 comp = new C1(0);
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comp).setState(State1.ONE);
//            }
//        }
//    }

    // 2

    public static class FindComponents2 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents2.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Object[] comps = new Object[]{new C1(0), new C2(0)};
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comps).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator = entityRepository.findCompositionsWith(C1.class, C2.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp2());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator = entityRepository.findCompositionsWith(C1.class, C2.class).withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp2());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class).withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents2FromMoreCompositions extends FindComponents2 {
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents2FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            Object[] comps = new Object[]{new C1(0), new C2(0)};
//            Object[] comps2 = new Object[]{new C1(0), new C2(0), new C3(0)};
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps).setState(State1.ONE);
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//        }
//    }

    // 3

    public static class FindComponents3 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents3.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0)};
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comps).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp3());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class).withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp3());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class).withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents3FromMoreCompositions extends FindComponents3 {
//        Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0)};
//        Object[] comps2 = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};
//
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents3FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps).setState(State1.ONE);
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//        }
//    }

    // 4

    public static class FindComponents4 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents4.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comps).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp4());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class)
                            .withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp4());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class)
                            .withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents4FromMoreCompositions extends FindComponents4 {
//        Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0)};
//        Object[] comps2 = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0), new C5(0)};
//
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents4FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps).setState(State1.ONE);
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//        }
//    }

    // 5

    public static class FindComponents5 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents5.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Object[] comps = new Object[]{
                    new C1(0), new C2(0), new C3(0),
                    new C4(0), new C5(0)
            };
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comps).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp5());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class)
                            .withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp5());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class)
                            .withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents5FromMoreCompositions extends FindComponents5 {
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents5FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0), new C5(0)};
//            Object[] comps2 = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0), new C5(0), new C6(0)};
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps).setState(State1.ONE);
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//        }
//    }

    // 6

    public static class FindComponents6 extends FindComponents {

        public static void main(String[] args) throws Exception {
            org.openjdk.jmh.Main.main(
                    new String[]{fetchBenchmarkName(FindComponents6.class)}
            );
        }

        @Override
        public void setupImpl() {
            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
            Object[] comps = new Object[]{
                    new C1(0), new C2(0), new C3(0),
                    new C4(0), new C5(0), new C6(0),
            };
            for (int i = 0; i < size; i++) {
                entityRepository.createEntity(comps).setState(State1.ONE);
            }
        }

        @Override
        public void iterateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp6());
            }
        }

        @Override
        public void iterateWithStateImpl(Blackhole bh) {
            var iterator =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class)
                            .withState(State1.ONE).iterator();
            while (iterator.hasNext()) {
                bh.consume(iterator.next().comp6());
            }
        }

        @Override
        public void streamImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).stream();
            stream.forEach(bh::consume);
        }

        @Override
        public void streamWithStateImpl(Blackhole bh) {
            var stream =
                    entityRepository.findCompositionsWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class)
                            .withState(State1.ONE).stream();
            stream.forEach(bh::consume);
        }
    }
//
//    public static class FindComponents6FromMoreCompositions extends FindComponents6 {
//        public static void main(String[] args) throws Exception {
//            org.openjdk.jmh.Main.main(
//                    new String[]{fetchBenchmarkName(FindComponents6FromMoreCompositions.class)}
//            );
//        }
//
//        @Override
//        public void setupImpl() {
//            entityRepository = (EntityRepository) new EntityRepository.Factory().create();
//            Object[] comps = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0), new C5(0), new C6(0)};
//            Object[] comps2 = new Object[]{new C1(0), new C2(0), new C3(0), new C4(0), new C5(0), new C6(0), new C7(0)};
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps).setState(State1.ONE);
//
//            }
//            for (int i = 0; i < size >> 1; i++) {
//                entityRepository.createEntity(comps2).setState(State1.ONE);
//            }
//        }
//    }
}
