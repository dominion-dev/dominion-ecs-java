package dev.dominion.ecs.engine.collections;

import java.util.Iterator;

public final class ObjectIterator<V> implements Iterator<V> {

    private final V[] data;
    private final int limit;
    int next = 0;

    ObjectIterator(V[] data, int limit) {
        this.data = data;
        this.limit = limit;
    }

    @Override
    public boolean hasNext() {
        return next < limit;
    }

    @Override
    public V next() {
        return data[next++];
    }
}
