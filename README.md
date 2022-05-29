# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100">|) () |\\/| | |\\| | () |\\|

[![Java CI with Maven](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/ci-maven.yml/badge.svg)](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/ci-maven.yml)
![CodeQL](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/codeql-analysis.yml/badge.svg)

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

## Features

- 🚀 **_FAST_** > Dominion is not only an insanely fast ECS implemented in Java, it can also be in the same league as
  ECS for C, C++, and Rust -
  see [benchmarks](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-engine-benchmarks/README.md)
  .
- 🤏 **_TINY_** > Just a high-performance and high-concurrency Java library with a minimal footprint and **no
  dependencies**. So you can easily integrate the Dominion core library into your game engine or framework or use it
  directly for your game or application without warring about the _dependency hell_.
- ☕️ **_SIMPLE_** > Dominion promotes a clean, minimal and self-explanatory API that is simple by design. A few readme
  pages will provide complete usage documentation.
- 💪 _with **SUPPORT**_ > [Join the Discord!](https://discord.gg/BHMz3axqUG) The server will support users and announce
  the availability of the new version.

## Quick Start

In your local environment you must have already installed a Java 17 (or newer) and Maven.

Add the following dependency declaration in your project pom.xml:

```xml

<project>
    <!-- ... -->

    <dependencies>
        <dependency>
            <groupId>dev.dominion.ecs</groupId>
            <artifactId>dominion-ecs-engine</artifactId>
            <version>0.6.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>central-snapshot</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
</project>
```

Check out the
[Dominion API documentation](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-api/README.md)
as a reference to get started implementing your first app.

## About Performance

Designing a high-performance and high-concurrency Java library is ~~a piece of cake~~ quite a challenge as the execution
speed of Java code could be affected in many ways. Dominion mitigates Java's performance pitfalls by taking a few
actions:

- **_not just using the standard library_**: the Java standard library implements data structures and algorithms
  designed without making any assumption about the data as they are general purpose. Dominion implements some custom
  data structures and algorithms to increase performances and fill the gap with ECS frameworks written in system
  languages.
- **_reducing garbage collection activities_**: GC could affect overall performances as its activities run concurrently
  with user code and without direct control. To reduce GC activities significantly, Dominion implements a very efficient
  pooling system to reuse objects living in a heap and creates off-heap data structures whenever possible.
- **_mastering concurrency_**: an ECS library must be not only fast but able to scale running on a multicore CPU.
  Otherwise, it makes little sense today. Writing a highly concurrent library in Java requires non-blocking concurrency
  as much as possible and leveraging the best available lock implementation as the
  fast [StampedLock](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/StampedLock.html).
- **_using Java 17_**: only by upgrading to the Java 17 you will get a performance boost for free: Java 17 is about 8-9%
  faster than Java 11. Whenever possible and to further reduce memory footprint, the Dominion
  uses [record classes](https://docs.oracle.com/en/java/javase/15/language/records.html) instead of standard classes to
  map more frequent objects.
- **_adding a blazing-fast logging layer_**: by implementing a thin logging layer over the
  standard [System.Logger](https://openjdk.java.net/jeps/264) (Platform Logging API and Service - JEP 264), Dominion
  achieves a half nanosecond logging level check with next to no performance impact and does not require a specific
  dependency on the logging implementation of your choice.

## Ok, enough blah blah blah..

Here is an example:

```java
public class HelloDominion {

    public static void main(String[] args) {
        // creates your world
        Dominion hello = Dominion.create();

        // creates an entity with components
        hello.createEntity(
                "my-entity",
                new Position(0, 0),
                new Velocity(1, 1)
        );

        // creates a system
        Runnable system = () -> {
            //finds entities
            hello.findEntitiesWith(Position.class, Velocity.class)
                    // stream the results
                    .stream().forEach(result -> {
                        Position position = result.comp1();
                        Velocity velocity = result.comp2();
                        position.x += velocity.x;
                        position.y += velocity.y;
                        System.out.printf("Entity %s moved with %s to %s\n",
                                result.entity().getName(), velocity, position);
                    });
        };

        // creates a scheduler
        Scheduler scheduler = hello.createScheduler();
        // schedules the system
        scheduler.schedule(system);
        // starts 3 ticks per second
        scheduler.tickAtFixedRate(3);
    }

    // component types can be both classes and records

    static class Position {
        double x, y;

        public Position(double x, double y) {/*...*/}

        @Override
        public String toString() {/*...*/}
    }

    record Velocity(double x, double y) {
    }
}
```

## Dominion Examples Page

Dominion comes with some documented sample apps to help adopt the solution, like the **HelloDominion** sample above.

The **DarkEntities** sample app has also been added, it could inspire a turn-based rogue-like game running on a terminal
window:

<img src="https://raw.githubusercontent.com/dominion-dev/dominion-ecs-java/main/dominion-ecs-examples/dark-entities-01.gif">

Here
the [Dominion Examples](https://github.com/dominion-dev/dominion-ecs-java/blob/main/dominion-ecs-examples/README.md)
page with all the documented code.

## Dominion Release Cycle

| Phase                  | Description                                                                                             | Distribution    | Git tag      |
|------------------------|---------------------------------------------------------------------------------------------------------|-----------------|--------------|
| Preview                | Features are under heavy development and often have changing requirements and scope.                    | github-zip only | none         |
| Early Access (EA)      | Features are documented and ready for testing with a wider audience.                                    | maven SNAPSHOT  | release-EA-# |
| Release Candidate (RC) | Features have been tested through one or more early access cycles with no known showstopper-class bugs. | maven RC        | release-RC#  |
| Stable Release         | Features have passed all verifications / tests. Stable releases are ready for production use            | maven RELEASE   | release      |

Dominion is officially in _**Early Access**_.

[Join the Discord for further updates!](https://discord.gg/BHMz3axqUG)

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
