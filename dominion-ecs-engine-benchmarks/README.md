# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion Engine Benchmarks

Below is a list of the most significant benchmarks related to the current implementation of the API. More benchmarks
will come as new API methods are implemented in the Dominion Engine.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                                 | Average Time |
|:---------------------------------------------------|:------------:|
| **Create** entities with **no** component          |   0.017 s    |
|                                                    |              |
| **Create** entities with **1** component           |   0.035 s    |
| **Create** entities with **2** components          |   0.049 s    |
| **Create** entities with **4** components          |   0.066 s    |
| **Create** entities with **8** components          |   0.097 s    |
|                                                    |              |
| **Create** prepared entities with **1** component  |   0.019 s    |
| **Create** prepared entities with **2** components |   0.027 s    |
| **Create** prepared entities with **4** components |   0.041 s    |
| **Create** prepared entities with **8** components |   0.059 s    |
|                                                    |              |
| **Delete** entities with **1** component           |   0.019 s    |
| **Delete** entities with **2** components          |   0.021 s    |
| **Delete** entities with **4** components          |   0.021 s    |
| **Delete** entities with **8** components          |   0.028 s    |
|                                                    |              |

| 1,000,000 Entities                         | Average Time |
|--------------------------------------------|:------------:|
| **Add** up to **1** component              |   0.051 s    |
| **Add** up to **2** components             |   0.059 s    |
| **Add** up to **4** components             |   0.070 s    |
| **Add** up to **8** components             |   0.095 s    |
|                                            |              |
| **Remove** from **1** component            |   0.051 s    |
| **Remove** from **2** components           |   0.059 s    |
| **Remove** from **4** components           |   0.068 s    |
| **Remove** from **8** components           |   0.090 s    |
|                                            |              |
| **Modify** with **1** component            |   0.047 s    |
| **Modify** with **2** components           |   0.051 s    |
| **Modify** with **4** components           |   0.060 s    |
| **Modify** with **8** components           |   0.083 s    |
|                                            |              |
| **SetState** with **1** component          |   0.047 s    |
| **SetState** with **2** component          |   0.046 s    |
| **SetState** with **4** components         |   0.046 s    |
| **SetState** with **8** components         |   0.046 s    |
|                                            |              |
| **Enable** entities with **1** component   |   0.025 s    |
| **Enable** entities with **2** components  |   0.026 s    |
| **Enable** entities with **4** components  |   0.032 s    |
| **Enable** entities with **8** components  |   0.042 s    |
|                                            |              |
| **Disable** entities with **1** component  |   0.025 s    |
| **Disable** entities with **2** components |   0.029 s    |
| **Disable** entities with **4** components |   0.038 s    |
| **Disable** entities with **8** components |   0.045 s    |
|                                            |              |
| **Has** with **1** component type          |   0.007 s    |
| **Has** with **2** component types         |   0.009 s    |
| **Has** with **4** component types         |   0.011 s    |
| **Has** with **8** component types         |   0.014 s    |
|                                            |              |
| **Contains** with **1** component          |   0.009 s    |
| **Contains** with **2** components         |   0.014 s    |
| **Contains** with **4** components         |   0.017 s    |
| **Contains** with **8** components         |   0.024 s    |
|                                            |              |

| 10,000,000 Entities                             | Average Time | Using _State_ - Average Time |
|-------------------------------------------------|:------------:|:----------------------------:|
| **Iterate** entities unpacking **1** component  |   0.007 s    |           0.041 s            |
| **Iterate** entities unpacking **2** components |   0.019 s    |           0.042 s            |
| **Iterate** entities unpacking **3** components |   0.023 s    |           0.082 s            |
| **Iterate** entities unpacking **4** components |   0.029 s    |           0.085 s            |
| **Iterate** entities unpacking **5** components |   0.034 s    |           0.085 s            |
| **Iterate** entities unpacking **6** components |   0.039 s    |           0.136 s            |
|                                                 |              |                              |
| **Stream** entities unpacking **1** component   |   0.023 s    |           0.050 s            |
| **Stream** entities unpacking **2** components  |   0.040 s    |           0.064 s            |
| **Stream** entities unpacking **3** components  |   0.052 s    |           0.079 s            |
| **Stream** entities unpacking **4** components  |   0.062 s    |           0.091 s            |
| **Stream** entities unpacking **5** components  |   0.075 s    |           0.106 s            |
| **Stream** entities unpacking **6** components  |   0.094 s    |           0.136 s            |
|                                                 |              |                              |

## Java and benchmarks

Implementing good benchmarks for libraries in Java is not an easy task.

There are several optimizations that the JVM might apply by running Java code in isolation, and the same optimizations
might not apply when the code is running as part of a larger application. This means that the wrong benchmarks could
lead you to believe that your Java code is performing much better than it actually is.

Fortunately, this is where  [Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh) comes to the rescue. JMH
is a project developed by the OpenJDK organization - guys who really know what they are doing - and it helps you stop
the JVM from applying optimizations when you start micro-benchmarking your Java code.

## Dominion benchmarks with JMH

To measure the actual performance of Dominion, this project provides several benchmark classes annotated with JMH, one
for each Dominion engine class that is critical to performance.

This means that you will find not only the benchmarks for the API interface implementation classes, but also the
benchmarks for the internal data structure and algorithms that are in the critical path.

If you want to run all the benchmarks on your local hardware, feel free to clone the project from git, build with Maven
and a JDK 17 of your choice, and run the `DominionBenchmark.All` main class (Dominion engine must be already built in
your local repository).

## Dominion VS Others

[Here](https://github.com/dominion-dev/dominion-ecs-java/tree/main/dominion-ecs-engine-benchmarks/OTHERS.md) you can
find a performance comparison between Dominion and other ECS implementations (like Artemis)

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
