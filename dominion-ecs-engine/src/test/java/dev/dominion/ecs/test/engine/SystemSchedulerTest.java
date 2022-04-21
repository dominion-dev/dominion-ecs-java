package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.engine.SystemScheduler;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class SystemSchedulerTest {

    @Test
    void schedule() {
        Scheduler scheduler = new SystemScheduler(ConfigSystem.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        int initialValue = 17, prime = 31;
        AtomicInteger count = new AtomicInteger(initialValue);
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 1));
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 2));
        scheduler.schedule(() -> count.getAndUpdate(value -> value * prime + 4));
        scheduler.tick();
        Assertions.assertEquals(((initialValue * prime + 1) * prime + 2) * prime + 4, count.get());
    }

    @Test
    void parallelSchedule() {
        Scheduler scheduler = new SystemScheduler(ConfigSystem.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
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
        Scheduler scheduler = new SystemScheduler(ConfigSystem.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
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
    void tickAtFixedRate() throws InterruptedException {
        Scheduler scheduler = new SystemScheduler(ConfigSystem.DEFAULT_SYSTEM_TIMEOUT_SECONDS, Context.TEST);
        AtomicInteger count = new AtomicInteger(0);
        scheduler.schedule(count::incrementAndGet);
        scheduler.tickAtFixedRate(60);
        Thread.sleep(100);
        Assertions.assertTrue(count.get() >= 6 && count.get() <= 7);
    }

    @Test
    void deltaTime() {
    }
}