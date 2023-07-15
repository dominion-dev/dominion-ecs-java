# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100">|) () |\\/| | |\\| | () |\\|

[![Java CI with Maven](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/ci-maven.yml/badge.svg)](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/ci-maven.yml)
![CodeQL](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/codeql-analysis.yml/badge.svg)

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java.

Entity Component System (ECS) architecture promotes data-oriented programming. It’s all about data (components) and
first-class functions (systems) that operate on data.

This means that, unlike OOP, data and operations are not encapsulated together in objects, which are called entities in
ECS.

Entities model the business objects of the user application, and the entity promotes "composition over inheritance" by
grouping a dynamic list of components to define its specific features.

Systems usually operate on components sequentially and can be very fast if data are stored in cache-friendly ways.
Systems are decoupled from each other and each system knows only about the data it operates on. This strongly promotes
high concurrency, running systems in parallel whenever they can independently operate on the data.

ECS architecture is particularly suitable (but not limited to) if you have to manage many objects in your application.
In addition, application code tends to be more reusable and easier to extend with new functionality thanks to the
components' composition and subsequent addition of new systems.

## Dominion Features

- 🚀 **_FAST_** > Dominion is not only an insanely fast ECS implemented in Java, it can also be in the same league as
  ECS for C, C++, and Rust -
  see [benchmarks](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-engine-benchmarks/README.md)
  .
- 🚀🚀 **_FASTER_** > Dominion is on average quite faster than other ECS implemented in Java. Check out
  this [performance comparison](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-engine-benchmarks/OTHERS.md)
  .
- 🤏 **_TINY_** > Just a high-performance and high-concurrency Java library with a minimal footprint and **no
  dependencies**. So you can easily integrate the Dominion core library into your game engine or framework or use it
  directly for your game or application without warring about the _dependency hell_.
- ☕️ **_SIMPLE_** > Dominion promotes a clean, minimal and self-explanatory API that is simple by design. A few readme
  pages will provide complete usage documentation.
- 💪 _with **SUPPORT**_ > [Join the Discord!](https://discord.gg/BHMz3axqUG) The server will support users and announce
  the availability of the new version.

## Archetypes

Dominion implements the so-called _Archetype_ through the <code>DataComposition</code> class, an aggregation of
component types
of which an entity can be made of.

In an ECS, the Archetype is a way to organize and manage entities based on their components. It provides a structured
approach for efficiently storing and querying entities with similar component compositions.

In an ECS, entities are composed of various components that define their behavior and characteristics. The Archetype
groups entities into archetypes based on their shared component types. An archetype represents a specific combination of
component types that entities possess.

The purpose of using archetypes is to optimize entity storage and system processing. Entities within the same archetype
have the same layout of components, which allows for data-oriented design and improves memory locality. This arrangement
enhances cache coherency, enables more efficient processing of system operations, and is primarily handled in Dominion
through the <code>ChunkedPool.Tenant</code> class.

Archetypes also facilitate efficient querying and iteration over entities that match specific component combinations. By
organizing entities based on their archetypes, systems can quickly identify and process only the relevant entities that
contain the required component types, reducing unnecessary overhead.

When a new entity is created or modified, the ECS manager checks the archetype it belongs to. If an existing archetype
matches the component composition, the entity is added to that archetype. Otherwise, a new archetype is created for the
entity's unique component combination.

By leveraging the Archetype, Dominion implementations can achieve high performance and scalability by
optimizing memory usage, facilitating cache-friendly access patterns, and enabling efficient processing of entities with
similar component compositions.

Overall, the Archetype in an Entity Component System provides an effective means of organizing, storing,
and processing entities with shared component types, leading to improved performance and flexibility in game development
and other related domains.

## Struct of Arrays

Dominion implements the specific Struct of Arrays (SoA) layout through the <code>ChunkedPool.LinkedChunk</code> class.

In an ECS, the SoA layout refers to a data organization approach where the components of entities are stored in separate
arrays based on their data types. This is in contrast to the traditional approach of storing entities as Arrays of
Structs (AoS) where all components of an entity are grouped together in a single struct.

The SoA layout is designed to improve data locality and cache efficiency, which can lead to significant performance
benefits in certain scenarios. By storing components of the same data type in contiguous arrays, the SoA layout allows
for better memory access patterns, reducing cache misses and improving CPU cache utilization.

For example, let's consider an ECS with three component types: Position, Velocity, and Renderable. In an SoA layout, all
Position components would be stored in a separate array, all Velocity components in another array, and so on. Each array
would contain the respective component data for the entities.

Overall, the SoA layout in Dominion provides a way to optimize memory access patterns and improve performance by storing
components in separate arrays based on their data types, allowing for efficient batch processing of components by
systems.

## Quick Start

In your local environment you must have already installed a Java 17 (or newer) and Maven.

Add the following dependency declaration in your project pom.xml:

```xml

<dependency>
    <groupId>dev.dominion.ecs</groupId>
    <artifactId>dominion-ecs-engine</artifactId>
    <version>0.9.0-RC2</version>
</dependency>
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
  with user code and without direct control. To reduce GC activities significantly, Dominion creates off-heap data
  structures whenever possible.
- **_mastering concurrency_**: an ECS library must be not only fast but able to scale running on a multicore CPU,
  otherwise, it would make little sense today.
- **_using Java 17_**: only by upgrading to the Java 17 you will get a performance boost for free: Java 17 is about 8-9%
  faster than Java 11.
- **_adding a blazing-fast logging layer_**: by implementing a thin logging layer over the
  standard [System.Logger](https://openjdk.java.net/jeps/264) (Platform Logging API and Service - JEP 264), Dominion
  achieves a half nanosecond logging level check with next to no performance impact and does not require a specific
  dependency on the logging implementation of your choice.

## Ok let's start

Here is a first example:

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

<img alt="dark-entities" src="https://raw.githubusercontent.com/dominion-dev/dominion-ecs-java/main/dominion-ecs-examples/dark-entities-01.gif">

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

Dominion is officially in _**Release Candidate**_.

[Join the Discord for further updates!](https://discord.gg/BHMz3axqUG)

## Credits

[![Java Profiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html)

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg" alt="IntelliJ Ultimate" width="75">](https://www.jetbrains.com/community/opensource/#support)

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
