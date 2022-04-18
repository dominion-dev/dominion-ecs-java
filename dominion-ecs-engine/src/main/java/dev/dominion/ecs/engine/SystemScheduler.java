/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Scheduler;

public class SystemScheduler implements Scheduler {

    @Override
    public Runnable schedule(Runnable system) {
        return null;
    }

    @Override
    public Runnable[] parallelSchedule(Runnable... systems) {
        return new Runnable[0];
    }

    @Override
    public void suspend(Runnable system) {
    }

    @Override
    public void resume(Runnable system) {
    }

    @Override
    public void tick() {
    }

    @Override
    public void tickAtFixedRate(int ticksPerSecond) {

    }

    @Override
    public double deltaTime() {
        return 0;
    }
}
