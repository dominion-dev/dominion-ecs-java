# <img src="dominion-logo-square.png" align="right" width="100">Dominion

[![Java CI with Maven](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml/badge.svg)](https://github.com/dominion-dev/dominion-ecs-java/actions/workflows/cicd-maven.yml)

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

## Features

- 🚀 **_fast_** : Dominion is not only an insanely fast ECS for the Java platform, it can also be in the same league as
  ECS for C/C++ (see [benchmarks](https://github.com/dominion-dev/dominion-ecs-java-benchmark))
- 🤏 **_tiny_**: just a high-performance Core Java library with a minimal footprint and no dependencies
- 🦾 **_easy_**: Dominion exposes a clean, self-explanatory API, and this readme alone will be enough to provide a
  complete usage documentation
- 🛠️ **_a pre-alpha_**: the project is still in early stages and the API is not yet complete and not fully implemented,
  but every part already built comes with unit tests and benchmarks. There are currently no releases yet and first
  release is scheduled for the first quarter of 2022.
- 👉 **_on discord_**: [join the Dominion Discord!](https://discord.gg/BHMz3axqUG)

## About Performance

Designing a high performance Java library is always a challenge as the execution speed of Java code could be affected in
many ways. Dominion mitigates Java performance pitfalls by setting a few key points:

- **_do not use only the standard library_**: the Java standard library implements data structures and algorithms
  designed without making any assumption about the data as they are general purpose. Dominion implements some custom
  data structures and algorithms to increase performances and fill the gap with C/C++ ECS frameworks.
- **_reduce garbage collection activities_**: GC could affect overall performances as its activities run concurrently
  with user code and without direct control. To reduce GC activities significantly, Dominion implements a pooling system
  to reuse arrays living in a heap and creates off-heap data structures whenever possible.
- **_mastering concurrency_**: an ECS library must be not only fast but able to scale running on a multicore CPU.
  Otherwise, it makes little sense. Writing a highly concurrent library in Java requires non-blocking concurrency and
  using the right tools. Dominion also implements concurrency using the powerful `StampedLock` introduced by Java 8.
- **_use Java 17 LTS_**: only by upgrading to the last JDK 17 you will get a performance boost for free. As already
  tested by [some users](https://www.optaplanner.org/blog/2021/09/15/HowMuchFasterIsJava17.html), Java 17 is about 8-9%
  faster than Java 11. Whenever possible and to further reduce memory footprint, the engine uses records (introduced by
  Java 14) instead of standard classes to map more frequent objects. Dominion started using Java 17 from the beginning
  and all benchmarks are run with this LTS version of Java.

## Getting Started

Dominion has not yet been released. The API is still under development and not yet ready to share detailed
documentation. The "how to" to get started with Dominion will come very soon, as soon as the very first version is
ready. [Join the Discord for updates!](https://discord.gg/BHMz3axqUG)

In the meantime, you can easily clone the repository and create a local build of the project and install it in your
local Maven repository. In your local environment you must have already installed a JDK 17 (of your choice) and Maven,
so in the root folder of the cloned project type the following command:

`mvn clean install`

With a build now available in your local repository you may
run [Dominion benchmarks](https://github.com/dominion-dev/dominion-ecs-java-benchmark) to independently check the
performance.