package dev.dominion.ecs.engine.collections;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * The ID bit schema:
 * <----------------------------- 64 ----------------------------->
 * <------------- 32 -------------><------------- 32 ------------->
 * <4 ><---- 14 ----><----- 16 -----><---- 14 ----><----- 16 ----->
 */
public final class ConcurrentPool<T> {

    private static final int NUM_OF_PAGES_BIT_SIZE = 14;
    private static final int PAGE_CAPACITY_BIT_SIZE = 16;
    private static final int NUM_OF_PAGES = 1 << NUM_OF_PAGES_BIT_SIZE;
    private static final int PAGE_INDEX_BIT_MASK = NUM_OF_PAGES - 1;
    private static final int PAGE_CAPACITY = 1 << PAGE_CAPACITY_BIT_SIZE;
    private static final int OBJECT_INDEX_BIT_MASK = PAGE_CAPACITY - 1;

    @SuppressWarnings("unchecked")
    private final Page<T>[] pages = new Page[NUM_OF_PAGES];
    private final AtomicInteger pageIndex = new AtomicInteger(0);

    private Page<T> newPage(Tenant<T> owner) {
        int id = pageIndex.getAndIncrement();
        Page<T> newPage = new Page<>(id, owner.currentPage);
        return pages[id] = newPage;
    }

    private Page<T> getPage(long id) {
        int pageId = (int) ((id >> PAGE_CAPACITY_BIT_SIZE) & PAGE_INDEX_BIT_MASK);
        return pages[pageId];
    }

    public Tenant<T> newTenant() {
        return new Tenant<>(this);
    }

    public static final class Tenant<T> {
        private final ConcurrentPool<T> pool;
        private final StampedLock lock = new StampedLock();
        private Page<T> currentPage;

        private Tenant(ConcurrentPool<T> pool) {
            this.pool = pool;
            currentPage = pool.newPage(this);
        }

        public long nextId() {
            long stamp = lock.writeLock();
            try {
                int pageSize = currentPage.hasCapacity() ?
                        currentPage.getAndIncrementSize() :
                        (currentPage = pool.newPage(this)).getAndIncrementSize();
                return (pageSize & OBJECT_INDEX_BIT_MASK) |
                        (currentPage.id & PAGE_INDEX_BIT_MASK) << PAGE_CAPACITY_BIT_SIZE;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    private static final class Page<T> {
        private final Object[] data = new Object[PAGE_CAPACITY];
        private final Page<T> previous;
        private final int id;
        private int size = 0;

        public Page(int id, Page<T> previous) {
            this.previous = previous;
            this.id = id;
        }

        public int getAndIncrementSize() {
            return size++;
        }

        @SuppressWarnings("unchecked")
        public T get(int key) {
            return (T) data[key];
        }

        public void set(int key, T value) {
            data[key] = value;
        }

        public boolean hasCapacity() {
            return size < data.length;
        }

        public Page<T> getPrevious() {
            return previous;
        }
    }
}
