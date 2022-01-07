package dev.dominion.ecs.engine.collections;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

public interface SparseIntMap<V> extends Cloneable {

    V put(int key, V value);

    V get(int key);

    Boolean contains(int key);

    V computeIfAbsent(int key, Function<Integer, ? extends V> mappingFunction);

    int size();

    boolean isEmpty();

    Iterator<V> iterator();

    Stream<V> stream();

    Stream<Integer> keysStream();

    void invalidateKeysHashCode();

    long sortedKeysHashCode();

    V[] values();

    int getCapacity();

    SparseIntMap<V> clone();

    @SuppressWarnings("ClassCanBeRecord")
    final class UnmodifiableView<V> implements SparseIntMap<V> {

        private final SparseIntMap<V> subject;

        private UnmodifiableView(SparseIntMap<V> subject) {
            this.subject = subject;
        }

        public static <T> SparseIntMap<T> wrap(SparseIntMap<T> subject) {
            return new UnmodifiableView<>(subject);
        }

        @Override
        public V put(int key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V get(int key) {
            return null;
        }

        @Override
        public Boolean contains(int key) {
            return null;
        }

        @Override
        public V computeIfAbsent(int key, Function<Integer, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return subject.size();
        }

        @Override
        public boolean isEmpty() {
            return subject.isEmpty();
        }

        @Override
        public Iterator<V> iterator() {
            return subject.iterator();
        }

        @Override
        public Stream<V> stream() {
            return subject.stream();
        }

        @Override
        public Stream<Integer> keysStream() {
            return subject.keysStream();
        }

        @Override
        public void invalidateKeysHashCode() {
            subject.invalidateKeysHashCode();
        }

        @Override
        public long sortedKeysHashCode() {
            return subject.sortedKeysHashCode();
        }

        @Override
        public V[] values() {
            return subject.values();
        }

        @Override
        public int getCapacity() {
            return subject.getCapacity();
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public SparseIntMap<V> clone() {
            return subject.clone();
        }
    }
}
