/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.engine.system.Logging;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SystemScheduler implements Scheduler {
    private static final System.Logger LOGGER = Logging.getLogger();
    private final int timeoutSeconds;
    private final Map<Runnable, Single> taskMap = new HashMap<>();
    private final List<Task> mainTasks = new ArrayList<>();
    private final ExecutorService mainExecutor;
    private final ForkJoinPool workStealExecutor;
    private final ScheduledExecutorService tickExecutor;
    private final Logging.Context loggingContext;
    private final StampedLock scheduleLock = new StampedLock();
    private final ReentrantLock tickLock = new ReentrantLock();
    private ScheduledFuture<?> scheduledTicks;
    private int currentTicksPerSecond = 0;
    private TickTime tickTime;

    public SystemScheduler(int timeoutSeconds, Logging.Context loggingContext) {
        this.timeoutSeconds = timeoutSeconds;
        this.loggingContext = loggingContext;
        var threadFactory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                SchedulerThread schedulerThread = new SchedulerThread(r);
                if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                    LOGGER.log(System.Logger.Level.DEBUG, "New scheduler-thread: " + schedulerThread.getName());
                }
                return schedulerThread;
            }
        };
        mainExecutor = Executors.newSingleThreadExecutor(threadFactory);
        tickExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        int nThreads = Runtime.getRuntime().availableProcessors();
        workStealExecutor = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "Parallel executor created with max {0} thread count", nThreads);
        }
        tickTime = new TickTime(System.nanoTime(), 1);
    }

    private static TickTime calcTickTime(TickTime currentTickTime) {
        long prevTime = currentTickTime.time;
        long currentTime = System.nanoTime();
        return new TickTime(currentTime, currentTime - prevTime);
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
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
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
                    var cluster = new Cluster(systems);
                    mainTasks.add(cluster);
                    taskMap.putAll(cluster.taskMap);
                    if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                        LOGGER.log(System.Logger.Level.DEBUG, "Schedule {0} parallel-systems in #{1} position", systems.length, mainTasks.size());
                    }
                }
            }
            return systems;
        } finally {
            scheduleLock.unlockWrite(stamp);
        }
    }

    public void forkAndJoin(Runnable subsystem) {
        Thread currentThread = Thread.currentThread();
        if (!(currentThread instanceof SchedulerThread || currentThread instanceof ForkJoinWorkerThread)) {
            throw new IllegalCallerException("Cannot invoke the forkAndJoin() method from outside other systems.");
        }
        try {
            workStealExecutor.invoke(new RecursiveAction() {
                @Override
                protected void compute() {
                    subsystem.run();
                }
            });
        } catch (RuntimeException ex) {
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.ERROR)) {
                LOGGER.log(System.Logger.Level.ERROR, "invoke", ex);
            }
        }
    }

    public void forkAndJoinAll(Runnable... subsystems) {
        if (!(Thread.currentThread() instanceof ForkJoinWorkerThread)) {
            throw new IllegalCallerException("Cannot invoke the forkAndJoinAll() method from outside other subsystems.");
        }
        ForkJoinTask.invokeAll(Arrays.stream(subsystems).map(system -> new RecursiveAction() {

            @Override
            protected void compute() {
                system.run();
            }
        }).toArray(ForkJoinTask[]::new));
    }

    @Override
    public void suspend(Runnable system) {
        Single singleTask = taskMap.get(system);
        if (singleTask == null) {
            return;
        }
        singleTask.setEnabled(false);
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
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
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "A system has been resumed");
        }
    }

    @Override
    public void tick() {
        tickLock.lock();
        try {
            tickTime = calcTickTime(tickTime);
            var futures = mainExecutor.invokeAll(mainTasks);
            futures.get(0).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.ERROR)) {
                LOGGER.log(System.Logger.Level.ERROR, "tick", ex);
            }
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
                } catch (InterruptedException | ExecutionException | TimeoutException | CancellationException ignored) {
                }
                scheduledTicks = null;
            }
            currentTicksPerSecond = ticksPerSecond;
            if (currentTicksPerSecond == 0) {
                return;
            }
            scheduledTicks = tickExecutor.scheduleAtFixedRate(this::tick, 0, 1000 / currentTicksPerSecond, TimeUnit.MILLISECONDS);
        } finally {
            tickLock.unlock();
        }
    }

    @Override
    public double deltaTime() {
        return tickTime.deltaTime / 1_000_000_000d;
    }

    @Override
    public boolean shutDown() {
        tickExecutor.shutdown();
        mainExecutor.shutdown();
        workStealExecutor.shutdown();
        try {
            return mainExecutor.awaitTermination(
                    timeoutSeconds, TimeUnit.SECONDS) &&
                    workStealExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS) &&
                    tickExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.ERROR)) {
                LOGGER.log(System.Logger.Level.ERROR, "shutdown", ex);
            }

        }
        return false;
    }

    private interface Task extends Callable<Void> {
    }

    record TickTime(long time, long deltaTime) {
    }

    private static final class SchedulerThread extends Thread {
        private static final AtomicInteger counter = new AtomicInteger(0);

        public SchedulerThread(Runnable runnable) {
            super(runnable, "dominion-scheduler-" + counter.getAndIncrement());
        }
    }

    private final class Single implements Task {
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
            if (isEnabled()) {
                forkAndJoin(system);
            }
            return null;
        }

        private void directRun() {
            if (isEnabled()) {
                system.run();
            }
        }
    }

    private final class Cluster implements Task {
        private final List<Single> tasks;
        private final Map<Runnable, Single> taskMap;

        private Cluster(Runnable[] systems) {
            tasks = Arrays.stream(systems).map(Single::new).toList();
            taskMap = tasks.stream().collect(Collectors.toMap(Single::getSystem, Function.identity()));
        }

        @Override
        public Void call() {
            forkAndJoin(() -> ForkJoinTask.invokeAll(tasks.stream().map(single -> new RecursiveAction() {

                @Override
                protected void compute() {
                    single.directRun();
                }
            }).toArray(ForkJoinTask[]::new)));
            return null;
        }
    }
}
