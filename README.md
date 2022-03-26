# <img src="dominion-logo-square.png" align="right" width="100">|) () |\\/| | |\\| | () |\\|

[![Java CI with Maven](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml/badge.svg)](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml)

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

## Features

- :rocket: **_FAST_** : Dominion is not only an insanely fast ECS implemented in Java, it can also be in the same league
  as ECS for C, C++, and Rust -
  see [benchmarks](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-engine-benchmark/README.md).
- 🤏 **_TINY_** : just a high-performance and high-concurrency Java library with a minimal footprint and **no
  dependencies**. So you can easily integrate the Dominion core library into your game engine or framework or use it
  directly for your game or application without warring about the _dependency hell_.
- :coffee: **_SIMPLE_** : Dominion promotes a clean, minimal and self-explanatory API that is simple by design. This
  readme alone will provide a complete usage documentation.
- :muscle: **_with SUPPORT_** : [join the Discord!](https://discord.gg/BHMz3axqUG) The server will support users and
  announce the availability of the new version.

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

## Ok, enough blah blah blah... Show me some code!

## Getting Started

In your local environment you must have already installed a JDK 17 (or newer) and Maven.

Clone the repository by following the GitHub Clone instructions and then install with the following Maven command:

```
mvn clean install`
```

With Dominion now installed in your local folder, you can first of all run the example code with the following commands:

```
java -jar dominion-ecs-example/target/hello-dominion.jar
```

## Dominion Release Cycle

| Phase                  | Description                                                                                             |
|------------------------|---------------------------------------------------------------------------------------------------------|
| Preview                | Features are under heavy development and often have changing requirements and scope.                    |
| Early Access (EA)      | Features are documented and ready for testing with a wider audience.                                    |
| Release Candidate (RC) | Features have been tested through one or more early access cycles with no known showstopper-class bugs. |
| Stable Release         | Features have passed all verifications / tests. Stable releases are ready for production use            |

Dominion is now a _**Preview**_.

[Join the Discord for further updates!](https://discord.gg/BHMz3axqUG)

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
