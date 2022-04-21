/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SystemScheduler implements Scheduler {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final int timeoutSeconds;
    private final Map<Runnable, Single> taskMap = new HashMap<>();
    private final List<Task> mainTasks = new ArrayList<>();
    private final ExecutorService mainExecutor;
    private final ExecutorService parallelExecutor;
    private final ScheduledExecutorService tickExecutor;
    private final LoggingSystem.Context loggingContext;
    private final StampedLock scheduleLock = new StampedLock();
    private final ReentrantLock tickLock = new ReentrantLock();
    private ScheduledFuture<?> scheduledTicks;
    private int currentTicksPerSecond = 0;

    public SystemScheduler(int timeoutSeconds, LoggingSystem.Context loggingContext) {
        this.timeoutSeconds = timeoutSeconds;
        this.loggingContext = loggingContext;
        var threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                String threadName = "dominion-scheduler-" + counter.getAndIncrement();
                if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                    LOGGER.log(System.Logger.Level.DEBUG, "New thread: " + threadName);
                }
                return new Thread(r, threadName);
            }
        };
        mainExecutor = Executors.newSingleThreadExecutor(threadFactory);
        tickExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        int nThreads = Runtime.getRuntime().availableProcessors();
        parallelExecutor = Executors.newFixedThreadPool(nThreads, threadFactory);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "Parallel executor created with max {0} thread count", nThreads);
        }
    }

    @Override
    public Runnable schedule(Runnable system) {
        long stamp = scheduleLock.writeLock();
        try {
            taskMap.computeIfAbsent(system, sys -> {
                Single single = new Single(sys);
                mainTasks.add(single);
                return single;
            });
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(System.Logger.Level.DEBUG, "Schedule a new system in #{0} position", mainTasks.size());
            }
            return system;
        } finally {
            scheduleLock.unlockWrite(stamp);
        }
    }

    @Override
    public Runnable[] parallelSchedule(Runnable... systems) {
        long stamp = scheduleLock.writeLock();
        try {
            switch (systems.length) {
                case 0:
                    return systems;
                case 1:
                    schedule(systems[0]);
                default: {
                    var cluster = new Cluster(systems, parallelExecutor, timeoutSeconds);
                    mainTasks.add(cluster);
                    taskMap.putAll(cluster.taskMap);
                    if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                        LOGGER.log(System.Logger.Level.DEBUG
                                , "Schedule {0} parallel-systems in #{1} position", systems.length, mainTasks.size());
                    }
                }
            }
            return systems;
        } finally {
            scheduleLock.unlockWrite(stamp);
        }
    }

    @Override
    public void suspend(Runnable system) {
        Single singleTask = taskMap.get(system);
        if (singleTask == null) {
            return;
        }
        singleTask.setEnabled(false);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "A system has been suspended");
        }
    }

    @Override
    public void resume(Runnable system) {
        Single singleTask = taskMap.get(system);
        if (singleTask == null) {
            return;
        }
        singleTask.setEnabled(true);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "A system has been resumed");
        }
    }

    @Override
    public void tick() {
        tickLock.lock();
        try {
            var futures = mainExecutor.invokeAll(mainTasks);
            futures.get(0).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            tickLock.unlock();
        }
    }

    @Override
    public void tickAtFixedRate(int ticksPerSecond) {
        tickLock.lock();
        try {
            if (scheduledTicks != null && ticksPerSecond != currentTicksPerSecond) {
                try {
                    scheduledTicks.cancel(false);
                    scheduledTicks.get(timeoutSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                }
                scheduledTicks = null;
            }
            currentTicksPerSecond = ticksPerSecond;
            if (currentTicksPerSecond == 0) {
                return;
            }
            scheduledTicks = tickExecutor
                    .scheduleAtFixedRate(this::tick, 0, 1000 / currentTicksPerSecond, TimeUnit.MILLISECONDS);
        } finally {
            tickLock.unlock();
        }
    }

    @Override
    public double deltaTime() {
        return 0;
    }

    private interface Task extends Callable<Void> {
    }

    private static class Single implements Task {
        private final Runnable system;
        private final AtomicBoolean enabled = new AtomicBoolean(true);

        public Single(Runnable system) {
            this.system = system;
        }

        public Runnable getSystem() {
            return system;
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public void setEnabled(boolean enabled) {
            this.enabled.set(enabled);
        }

        @Override
        public Void call() {
            if (!isEnabled()) {
                return null;
            }
            system.run();
            return null;
        }
    }

    private static class Cluster implements Task {
        private final List<Single> tasks;
        private final Map<Runnable, Single> taskMap;
        private final ExecutorService parallelExecutor;
        private final int timeoutSeconds;

        private Cluster(Runnable[] systems, ExecutorService parallelExecutor, int timeoutSeconds) {
            tasks = Arrays.stream(systems).map(Single::new).toList();
            taskMap = tasks.stream().collect(Collectors.toMap(Single::getSystem, Function.identity()));
            this.parallelExecutor = parallelExecutor;
            this.timeoutSeconds = timeoutSeconds;
        }

        @Override
        public Void call() throws Exception {
            var futures = parallelExecutor.invokeAll(tasks);
            try {
                futures.get(0).get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
