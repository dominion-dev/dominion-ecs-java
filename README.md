# <img src="dominion-logo-square.png" align="right" width="100">Dominion

[![Java CI with Maven](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml/badge.svg)](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml)

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

## Features

- :rocket: **_FAST_** : Dominion is not only an insanely fast ECS for the Java platform, it can also be in the same league as
  ECS for C/C++ (see [benchmarks](https://github.com/dominion-dev/dominion-ecs-java-benchmark))
- 🤏 **_TINY_** : just a high-performance Core Java library with a minimal footprint and no dependencies. So you can
  easily integrate Dominion into your game engine or framework or use it directly for your game or application.
- :coffee: **_SIMPLE_** : Dominion aims to be simple by design and exposes a clean, minimal, and self-explanatory API. This
  readme alone will provide a complete usage documentation
- :muscle: **_with SUPPORT_** : [join the Discord!](https://discord.gg/BHMz3axqUG) The server will support users and announce
  the availability of the new version.

Dominion is still in early stages :baby_bottle:. The API is not yet complete and not fully implemented, but every part already
built comes with unit tests and benchmarks. There are currently no releases yet and first release is scheduled for the
first quarter of 2022.

## About Performance

Designing a high-performance Java library is quite a challenge as the execution speed of Java code could be affected in
many ways. Dominion mitigates Java's performance pitfalls by taking a few actions:

- **_not just using the standard library_**: the Java standard library implements data structures and algorithms
  designed without making any assumption about the data as they are general purpose. Dominion implements some custom
  data structures and algorithms to increase performances and fill the gap with C/C++ ECS frameworks.
- **_reducing garbage collection activities_**: GC could affect overall performances as its activities run concurrently
  with user code and without direct control. To reduce GC activities significantly, Dominion implements a pooling system
  to reuse arrays living in a heap and creates off-heap data structures whenever possible.
- **_mastering concurrency_**: an ECS library must be not only fast but able to scale running on a multicore CPU.
  Otherwise, it makes little sense today. Writing a highly concurrent library in Java requires non-blocking concurrency
  as much as possible and leveraging the best tools - Dominion also implements concurrency using the
  powerful [StampedLock](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/StampedLock.html)
  introduced in Java since version 8.
- **_using Java 17 LTS_**: only by upgrading to the last JDK 17 you will get a performance boost for free. As already
  tested by [some users](https://www.optaplanner.org/blog/2021/09/15/HowMuchFasterIsJava17.html), Java 17 is about 8-9%
  faster than Java 11. Whenever possible and to further reduce memory footprint, the engine
  uses [record classes](https://docs.oracle.com/en/java/javase/15/language/records.html) (introduced by Java 14 as a
  preview) instead of standard classes to map more frequent objects. Dominion started using Java 17 from the beginning
  and all benchmarks are run with this LTS version of Java.

## Getting Started

Dominion has not yet been released. The API is still under development and not yet ready to share detailed
documentation. The "how to" to get started with Dominion will come very soon, as soon as the first version is
ready. [Join the Discord for updates!](https://discord.gg/BHMz3axqUG)

In the meantime, you can easily clone the repository, create a local build of the project, and install it in your local
Maven repository. In your local environment, you must have already installed a JDK 17 (of your choice) and Maven. Then,
in the root folder of the cloned project, type the following command:

`mvn clean install`

With a Dominion build now available in your local repository you may build and run
the [Dominion benchmarks](https://github.com/dominion-dev/dominion-ecs-java-benchmark) to independently check the
performance.