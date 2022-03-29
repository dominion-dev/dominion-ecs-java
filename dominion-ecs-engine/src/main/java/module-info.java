import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.engine.EntityRepository;

module dev.dominion.ecs.engine {
    exports dev.dominion.ecs.engine
            to dev.dominion.ecs.test.engine, dev.dominion.ecs.engine.benchmarks;
    exports dev.dominion.ecs.engine.collections
            to dev.dominion.ecs.test.engine, dev.dominion.ecs.engine.benchmarks;
    exports dev.dominion.ecs.engine.system
            to dev.dominion.ecs.test.engine, dev.dominion.ecs.engine.benchmarks;
    provides Dominion.Factory with EntityRepository.Factory;

    requires dev.dominion.ecs.api;
    requires jdk.unsupported;
    requires java.logging;
}