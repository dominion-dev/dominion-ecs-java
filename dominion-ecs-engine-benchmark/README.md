# <img src="https://github.com/dominion-dev/dominion-ecs-java/raw/main/dominion-logo-square.png" align="right" width="100"> Dominion Benchmark

Below is a list of the most significant benchmarks related to the current implementation of the API. More benchmarks
will come as new API methods are implemented.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                              | Average Time |
|:------------------------------------------------|:------------:|
| **Create** entities with **no** component       |   0.018 s    |
| **Create** entities with **1** component        |   0.036 s    |
| **Create** entities with **2** components       |   0.048 s    |
| **Create** entities with **4** components       |   0.060 s    |
| **Create** entities with **8** components       |   0.080 s    |
| **Create** entities with **16** components      |   0.122 s    |
|                                                 |              |
| **Delete** entities with **any** comp.count     |   0.029 s    |
|                                                 |              |
| **En/Disable** entities with **any** comp.count |   0.009 s    |
|                                                 |              |

| 1,000,000 Entities                     | Average Time |
|----------------------------------------|:------------:|
| **Add** to **1** component             |   0.088 s    |
| **Add** to **2** components            |   0.095 s    |
| **Add** to **4** components            |   0.105 s    |
| **Add** to **8** components            |   0.160 s    |
| **Add** to **16** components           |   0.180 s    |
|                                        |              |
| **Remove** from **1** component        |   0.046 s    |
| **Remove** from **2** components       |   0.088 s    |
| **Remove** from **4** components       |   0.106 s    |
| **Remove** from **8** components       |   0.163 s    |
| **Remove** from **16** components      |   0.166 s    |
|                                        |              ||                                        |              |
| **Has** with **1** component type      |   0.008 s    |
| **Has** with **4** component types     |   0.012 s    |
| **Has** with **8** component types     |   0.012 s    |
| **Has** with **16** component types    |   0.012 s    |
|                                        |              |
| **Contains** with **1** component      |   0.009 s    |
| **Contains** with **4** components     |   0.016 s    |
| **Contains** with **8** components     |   0.017 s    |
| **Contains** with **16** components    |   0.017 s    |
|                                        |              |

| 10,000,000 Entities                        | Entities from <br/> Single _Composition_ <br/> - <br/>Average Time | Entities from <br/> More _Compositions_ <br/> - <br/> Average Time |
|--------------------------------------------|:------------------------------------------------------------------:|:------------------------------------------------------------------:|
| **Iterate** entities unpacking **1** comp. |                              0.032 s                               |                              0.050 s                               |
| **Iterate** entities unpacking **2** comp. |                              0.032 s                               |                              0.050 s                               |
| **Iterate** entities unpacking **3** comp. |                              0.032 s                               |                              0.052 s                               |
| **Iterate** entities unpacking **4** comp. |                              0.035 s                               |                              0.054 s                               |
| **Iterate** entities unpacking **5** comp. |                              0.037 s                               |                              0.058 s                               |
| **Iterate** entities unpacking **6** comp. |                              0.054 s                               |                              0.061 s                               |
|                                            |                                                                    |                                                                    |


Implementing good benchmarks for Java libraries is not an easy task.

There are several optimizations that the JVM might apply by running Java code in isolation, and the same optimizations
might not apply when the code is running as part of a larger application. This means that the wrong benchmarks could
lead you to believe that your Java code is performing much better than it actually is.

Fortunately, this is where  [Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh) comes to the rescue. JMH
is a project developed by the OpenJDK organization - guys who really know what they are doing - and it helps you stop
the JVM from applying optimizations when you start micro-benchmarking your Java code.

## Dominion benchmarks with JMH

To measure the actual performance of [Dominion](https://github.com/dominion-dev/dominion-ecs-java), this project
provides several benchmark classes annotated with JMH, one for each Dominion engine class that is critical to
performance.

This means that you will find not only the benchmarks for the API interface implementation classes, but also the
benchmarks for the internal data structure and algorithms that are in the critical path.

If you want to run all the benchmarks on your local hardware, feel free to clone the project from git, build with Maven
and a JDK 17 of your choice, and run the `DominionBenchmark.All` main
class ([Dominion](https://github.com/dominion-dev/dominion-ecs-java) engine must be already built in your local
repository).

## Support Dominion
If you want to support Dominion project, consider giving it a **Star** ⭐️
