module dev.dominion.ecs.test.engine {
    requires dev.dominion.ecs.engine;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    opens dev.dominion.ecs.test.engine
            to org.junit.platform.commons;
    opens dev.dominion.ecs.test.engine.collections
            to org.junit.platform.commons;
    opens dev.dominion.ecs.test.engine.system
            to org.junit.platform.commons;
}