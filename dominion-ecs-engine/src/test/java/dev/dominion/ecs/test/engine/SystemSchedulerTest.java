package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.engine.SystemScheduler;
import dev.dominion.ecs.engine.system.Config;
import dev.dominion.ecs.engine.system.Logging.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class SystemSchedulerTest {

    @Test
    void schedule() {
        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        int initialValue = 17, prime = 31;
        AtomicInteger count = new AtomicInteger(initialValue);
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 1));
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 2));
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 4));
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * prime + 2) * prime + 4, count.get());
    }

    @Test
    void scheduleWithException() {
        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        Runnable systemA = scheduler.schedule(() -> {
            throw new RuntimeException("Test system runtime exception");
        });
        scheduler.tick();
        scheduler.suspend(systemA);
        scheduler.schedule(() -> {
            System.out.println("run subsystem");
            scheduler.forkAndJoin(
                    () -> {
                        throw new RuntimeException("Test subsystem runtime exception");
                    }
            );
        });
        scheduler.schedule(() -> {
            System.out.println("run subsystem in parallel");
            scheduler.forkAndJoinAll(
                    () -> {
                        throw new RuntimeException("Test subsystem-A runtime exception");
                    },
                    () -> {
                        throw new RuntimeException("Test subsystem-B runtime exception");
                    }
            );
        });
        scheduler.tick();
    }

    @Test
    void parallelSchedule() {
        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        int initialValue = 17, prime = 31;
        AtomicInteger count = new AtomicInteger(initialValue);
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 1));
        scheduler.parallelSchedule(
                () -> count.getAndUpdate(value -> value * 2),
                () -> count.getAndUpdate(value -> value * 5),
                () -> count.getAndUpdate(value -> value * 7));
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 4));
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * 7 * 5 * 2) * prime + 4, count.get());
    }

    @Test
    void suspendAndResume() {
        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        int initialValue = 17, prime = 31;
        AtomicInteger count = new AtomicInteger(initialValue);
        Runnable runnable1 = scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 1));
        Runnable[] runnables = scheduler.parallelSchedule(
                () -> count.getAndUpdate(value -> value * 2),
                () -> count.getAndUpdate(value -> value * 5),
                () -> count.getAndUpdate(value -> value * 7));
        Runnable runnable2 = scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 4));

        scheduler.suspend(runnable1);
        scheduler.tick();
        Assertions.assertEquals(((initialValue) * 7 * 5 * 2) * prime + 4, count.get());

        count.set(initialValue);
        scheduler.resume(runnable1);
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * 7 * 5 * 2) * prime + 4, count.get());

        count.set(initialValue);
        scheduler.suspend(runnables[0]);
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * 7 * 5) * prime + 4, count.get());

        count.set(initialValue);
        scheduler.suspend(runnables[1]);
        scheduler.resume(runnables[0]);
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * 7 * 2) * prime + 4, count.get());

        count.set(initialValue);
        scheduler.suspend(runnable2);
        scheduler.resume(runnables[1]);
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * 7 * 5 * 2), count.get());
    }

    @Test
    void forkAndJoin() {
        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        int initialValue = 17, prime = 31;
        AtomicInteger count = new AtomicInteger(initialValue);
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 1));
        scheduler.schedule(() -> {
            scheduler.forkAndJoin(() -> count.getAndUpdate(value -> value * 2));
            count.getAndUpdate(value -> value * prime + 5);
        });
        scheduler.schedule(() -> {
            scheduler.forkAndJoinAll(
                    () -> count.getAndUpdate(value -> value * 7),
                    () -> count.getAndUpdate(value -> value * 11),
                    () -> count.getAndUpdate(value -> value * 13)
            );
            count.getAndUpdate(value -> value * prime + 17);
        });
        scheduler.tick();
        Assertions.assertEquals(
                ((((initialValue * prime + 1) * 2) * prime + 5) * 13 * 11 * 7) * prime + 17,
                count.get()
        );
    }

//    @Test
//    void tickAtFixedRate() throws InterruptedException {
//        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
//        AtomicInteger count = new AtomicInteger(0);
//        scheduler.schedule(count::incrementAndGet);
//        scheduler.tickAtFixedRate(60);
//        Thread.sleep(100);
//        scheduler.tickAtFixedRate(0);
//        int ticks = count.get();
//        double d = ticks / 10d;
//        Assertions.assertEquals(.6f, d, .1);
//        Thread.sleep(100);
//        Assertions.assertEquals(ticks, count.get());
//    }

//    @Test
//    void deltaTime() throws InterruptedException {
//        Scheduler scheduler = new SystemScheduler(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
//        AtomicReference<Double> count = new AtomicReference<>(0d);
//        scheduler.schedule(() -> count.set(count.get() + scheduler.deltaTime()));
//        scheduler.tickAtFixedRate(60);
//        Thread.sleep(100);
//        scheduler.tickAtFixedRate(0);
//        double d = count.get();
//        Assertions.assertEquals(0.1f, d, .07);
//        count.set(0d);
//        scheduler.tick(500_000_000);
//        d = count.get();
//        Assertions.assertEquals(0.5f, d, .0001);
//    }
}