/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

/**
 * A Scheduler provides methods to submit/suspend/resume systems that are executed on every tick.
 * Systems are defined as a plain old Java Runnable type, so they can be provided as lambda expressions and are
 * guaranteed to run sequentially.
 * Parallel systems run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.
 * A System, or a slot of parallel systems, can be suspended and resumed running at any time and preserving its
 * execution order.
 * Schedulers can start a periodic tick that becomes enabled immediately and subsequently with the given fixed rate.
 * A deltaTime method provides the time in seconds between the last tick and the current tick.
 */
public interface Scheduler {

    /**
     * Submits a system that becomes enabled immediately and executed on every tick.
     * Scheduled systems are guaranteed to execute sequentially, and no more than one task will be active at any given
     * time.
     *
     * @param system the system task to schedule
     * @return the scheduled system
     */
    Runnable schedule(Runnable system);

    /**
     * Submits systems that become enabled immediately and executed on every tick.
     * Parallel systems run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.
     *
     * @param systems list of systems to run concurrently in a slot
     * @return the slot of the underlying parallel systems that can be suspended and resumed as a whole
     */
    Runnable parallelSchedule(Runnable... systems);

    /**
     * Suspends an already scheduled system or a slot of parallel systems, preserving its execution order.
     *
     * @param system the system to be suspended
     */
    void suspend(Runnable system);


    /**
     * Resumes an already suspended system or a slot of parallel systems, in the original execution order
     *
     * @param system the system to be resumed
     */
    void resume(Runnable system);

    /**
     * Starts running all scheduled systems sequentially in a guaranteed order.
     * Systems sent in parallel run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.
     */
    void tick();

    /**
     * Starts a periodic tick that becomes enabled immediately and subsequently with the given fixed rate
     *
     * @param ticksPerSecond the required number of ticks per second or 0 if you want to pause the periodic tick.
     */
    void tickAtFixedRate(int ticksPerSecond);

    /**
     * DeltaTime is the time in seconds between the last tick and the current tick
     *
     * @return the deltaTime in seconds
     */
    double deltaTime();
}
