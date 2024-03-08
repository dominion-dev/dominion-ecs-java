package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Scheduler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author biding
 */
public class Test {
    public enum S {
        A, B
    }
    public record A(int id) {
    }
    public static void consume(Object c) {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        final var list = new ArrayList<Entity>(1000);
        for (int i = 0; i < 5000; i++) {
            list.add(entityRepository.createEntity(new A(i)).setState(S.A));
        }
        Scheduler scheduler = entityRepository.createScheduler();

        scheduler.schedule(() -> entityRepository.findEntitiesWith(A.class).withState(S.A).forEach(Test::consume));


        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    for (int i = 0; i < 2000; i++) {
                        list.get(i).setState(S.B);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        scheduler.tickAtFixedRate(20);
    }
}
