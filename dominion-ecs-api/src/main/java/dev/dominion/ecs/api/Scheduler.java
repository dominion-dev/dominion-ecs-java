/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

/**
 * A Scheduler that provides methods to submit/suspend/resume systems that are executed on every tick.
 * Systems are defined as a plain old Java Runnable type, so they can be provided as lambda expressions and are
 * guaranteed to run sequentially.
 * Parallel systems run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.
 * Systems, even if running parallel, can be suspended and resumed at any time, maintaining the order of execution.
 * A system can fork by creating several subsystems for immediate parallel executions and "join" while waiting for
 * all subsystems to execute.
 * Schedulers can start a periodic tick that becomes enabled immediately and subsequently with the given fixed rate.
 * A deltaTime method provides the time in seconds between the last tick and the current tick.
 * <p>
 * <pre>
 * schedule(A)
 * parallelSchedule(B,C)
 * schedule(D)
 * tickAtFixedRate(1)
 *
 * system A ---#---------------|*------|*------|*------|*----------
 * system B --------#----------|-*-----|-*-----|-*-----|-*---------
 * system C --------#----------|-*-----|-*-----|-*-----|-*---------
 * system D -------------#-----|--*----|--*----|--*----|--*--------
 *             |    |    |    tick0s  tick1s  tick2s  tickNs
 *            +A   +B,C  +D    |>
 *
 * suspend(B)
 * suspend(D)
 *
 * system A -|*--------------|*--------------|*-------
 * system B -|-*----X--------|---------------|--------
 * system C -|-*-------------|-*-------------|-*------
 * system D -|--*-------X----|---------------|--------
 *          tickNs  |   |   tickN+1s      tickN+2s
 *                 -B  -D
 *
 * resume(B)
 *
 * system A -|*----------|*-----------|*-------
 * system B -|------#----|-*----------|-*------
 * system C -|-*---------|-*----------|-*------
 * system D -|---------- |------------|--------
 *          tickNs  |   tickN+1s     tickN+2s
 *                 +B
 *
 * systemA.forkAndJoinAll(subsystemA1, subsystemA2)
 *
 * system A -|*_*--------|*_*--------|*_*--------|
 *  sub A1  -|-*---------|-*---------|-*---------|
 *  sub A2  -|-*---------|-*---------|-*---------|
 * system B -|---*-------|---*-------|---*-------|
 * system C -|---*-------|---*-------|---*-------|
 *             |           |           |
 *             *A1,A2     *A1,A2      *A1,A2
 * </pre>
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
     * @return the parallel systems that can be suspended and resumed individually
     */
    Runnable[] parallelSchedule(Runnable... systems);

    /**
     * A system can fork by creating a subsystem for immediate execution and "join" while waiting for the subsystem to execute.
     *
     * @param subsystem the new subsystem to run and wait until the end
     */
    void forkAndJoin(Runnable subsystem);

    /**
     * A system can fork by creating several subsystems for immediate parallel executions and "join" while waiting for
     * all subsystems to execute.
     *
     * @param subsystems the subsystems to run and wait until the end
     */
    void forkAndJoinAll(Runnable... subsystems);

    /**
     * Suspends an already scheduled system preserving its execution order.
     *
     * @param system the system to be suspended
     */
    void suspend(Runnable system);

    /**
     * Resumes an already suspended system in the original execution order.
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
     * DeltaTime is the time in seconds between the last tick and the current tick.
     *
     * @return the deltaTime in seconds
     */
    double deltaTime();

    /**
     * Initiates an orderly shutdown in which previously submitted systems are executed, but no new systems will be accepted.
     *
     * @return true if this scheduler terminated and false if the timeout elapsed before termination
     */
    boolean shutDown();
}
