package dev.dominion.ecs.engine.collections;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * The ID bit schema:
 * <----------------------------- 64 ----------------------------->
 * <------------- 32 -------------><------------- 32 ------------->
 * <4 ><---- 14 ----><----- 16 -----><---- 14 ----><----- 16 ----->
 */
public final class ConcurrentPool<T> {
    public static final int NUM_OF_PAGES_BIT_SIZE = 14;
    public static final int PAGE_CAPACITY_BIT_SIZE = 16;
    public static final int NUM_OF_PAGES = 1 << NUM_OF_PAGES_BIT_SIZE;
    public static final int PAGE_INDEX_BIT_MASK = NUM_OF_PAGES - 1;
    public static final long PAGE_INDEX_BIT_MASK_SHIFTED = (long) PAGE_INDEX_BIT_MASK << PAGE_CAPACITY_BIT_SIZE;
    public static final int PAGE_CAPACITY = 1 << PAGE_CAPACITY_BIT_SIZE;
    public static final int OBJECT_INDEX_BIT_MASK = PAGE_CAPACITY - 1;

    @SuppressWarnings("unchecked")
    private final LinkedPage<T>[] pages = new LinkedPage[NUM_OF_PAGES];
    private final AtomicInteger pageIndex = new AtomicInteger(-1);

    private LinkedPage<T> newPage(Tenant<T> owner) {
        int id = pageIndex.incrementAndGet();
        LinkedPage<T> newPage = new LinkedPage<>(id, owner.currentPage);
        return pages[id] = newPage;
    }

    private LinkedPage<T> getPage(long id) {
        int pageId = (int) ((id >> PAGE_CAPACITY_BIT_SIZE) & PAGE_INDEX_BIT_MASK);
        return pages[pageId];
    }

    public T getEntry(long id) {
        return getPage(id).get(id);
    }

    public Tenant<T> newTenant() {
        return new Tenant<>(this);
    }

    public int size() {
        return Arrays.stream(pages)
                .filter(Objects::nonNull)
                .mapToInt(LinkedPage::size)
//                .peek(System.out::println)
                .sum();
    }

    public static final class Tenant<T> implements AutoCloseable {
        private final ConcurrentPool<T> pool;
        private final StampedLock lock = new StampedLock();
        private final ConcurrentLongStack stack;
        private LinkedPage<T> currentPage;
        private long newId;

        private Tenant(ConcurrentPool<T> pool) {
            this.pool = pool;
            currentPage = pool.newPage(this);
            stack = new ConcurrentLongStack(1 << 16);
            nextId();
        }

        public long nextId() {
            long returnValue = stack.pop();
            if (returnValue != Long.MIN_VALUE) {
                return returnValue;
            }
            long stamp = lock.tryOptimisticRead();
            try {
                for (; ; ) {
                    if (stamp == 0L) {
                        stamp = lock.writeLock();
                        continue;
                    }
                    // possibly racy reads
                    returnValue = newId;
                    int pageIndex;
                    if (currentPage.hasCapacity()) {
                        pageIndex = currentPage.incrementIndex();
                        if (!lock.validate(stamp)) {
                            currentPage.decrementIndex();
                            stamp = lock.writeLock();
                            continue;
                        }
                    } else {
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp == 0L) {
                            stamp = lock.writeLock();
                            continue;
                        }
                        // exclusive access
                        pageIndex = (currentPage = pool.newPage(this)).incrementIndex();
                    }
                    newId = (pageIndex & OBJECT_INDEX_BIT_MASK) |
                            (currentPage.id & PAGE_INDEX_BIT_MASK) << PAGE_CAPACITY_BIT_SIZE;
                    return returnValue;
                }
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
        }

        public void freeId(long id) {
            LinkedPage<T> page = pool.getPage(id);
            if (page.isEmpty()) {
                stack.push(id);
                return;
            }
            boolean notCurrentPage = page != currentPage;
            int reusableId = page.remove(id, notCurrentPage);
            if (notCurrentPage) {
                stack.push((id & PAGE_INDEX_BIT_MASK_SHIFTED) | reusableId);
            } else {
                newId = reusableId;
            }
        }

        public T register(long id, T entry) {
            return pool.getPage(id).set(id, entry);
        }

        public int currentPageSize() {
            return currentPage.size();
        }

        @Override
        public void close() {
            stack.close();
        }
    }

    public static final class LinkedPage<T> {
        private final Object[] data = new Object[PAGE_CAPACITY];
        private final LinkedPage<T> previous;
        private final int id;
        private final AtomicInteger index = new AtomicInteger(-1);

        public LinkedPage(int id, LinkedPage<T> previous) {
            this.previous = previous;
            this.id = id;
        }

        public int incrementIndex() {
            return index.incrementAndGet();
        }

        public int decrementIndex() {
            return index.decrementAndGet();
        }

        public int remove(long id, boolean doNotUpdateIndex) {
            int indexToBeReused = (int) id & OBJECT_INDEX_BIT_MASK;
            for (; ; ) {
                int lastIndex = doNotUpdateIndex ? index.get() : index.decrementAndGet();
                if (lastIndex >= PAGE_CAPACITY) {
                    index.compareAndSet(PAGE_CAPACITY, PAGE_CAPACITY - 1);
                    continue;
                }
                if (lastIndex < 0) {
                    return 0;
                }
                data[indexToBeReused] = data[lastIndex];
                return lastIndex;
            }
        }

        @SuppressWarnings("unchecked")
        public T get(long id) {
            return (T) data[(int) id & OBJECT_INDEX_BIT_MASK];
        }

        @SuppressWarnings("unchecked")
        public T set(long id, T value) {
            return (T) (data[(int) id & OBJECT_INDEX_BIT_MASK] = value);
        }

        public boolean hasCapacity() {
            return index.get() < PAGE_CAPACITY - 1;
        }

        public LinkedPage<T> getPrevious() {
            return previous;
        }

        public int size() {
            return index.get() + 1;
        }

        public boolean isEmpty() {
            return size() == 0;
        }
    }
}
