import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.test.api.DominionTest;

module dev.dominion.ecs.test.api {
    requires dev.dominion.ecs.api;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    provides Dominion.Factory with DominionTest.MockDominionFactory;

    opens dev.dominion.ecs.test.api
            to org.junit.platform.commons;
}
