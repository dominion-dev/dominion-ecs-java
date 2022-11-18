# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion Engine Benchmarks

Below is a list of the most significant benchmarks related to the current implementation of the API. More benchmarks
will come as new API methods are implemented in the Dominion Engine.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                                 | Average Time |
|:---------------------------------------------------|:------------:|
| **Create** entities with **no** component          |   0.021 s    |
|                                                    |              |
| **Create** entities with **1** component           |   0.034 s    |
| **Create** entities with **2** components          |   0.047 s    |
| **Create** entities with **4** components          |   0.077 s    |
| **Create** entities with **8** components          |   0.096 s    |
|                                                    |              |
| **Create** prepared entities with **1** component  |   0.027 s    |
| **Create** prepared entities with **2** components |   0.031 s    |
| **Create** prepared entities with **4** components |   0.042 s    |
| **Create** prepared entities with **8** components |   0.078 s    |
|                                                    |              |
| **Delete** entities with **1** component           |   0.016 s    |
| **Delete** entities with **2** components          |   0.017 s    |
| **Delete** entities with **4** components          |   0.017 s    |
| **Delete** entities with **8** components          |   0.016 s    |
|                                                    |              |

| 1,000,000 Entities                         | Average Time |
|--------------------------------------------|:------------:|
| **Add** up to **1** component              |   0.054 s    |
| **Add** up to **2** components             |   0.055 s    |
| **Add** up to **4** components             |   0.062 s    |
| **Add** up to **8** components             |   0.084 s    |
|                                            |              |
| **Remove** from **1** component            |   0.049 s    |
| **Remove** from **2** components           |   0.052 s    |
| **Remove** from **4** components           |   0.059 s    |
| **Remove** from **8** components           |   0.092 s    |
|                                            |              |
| **Modify** with **1** component            |   0.039 s    |
| **Modify** with **2** components           |   0.046 s    |
| **Modify** with **4** components           |   0.051 s    |
| **Modify** with **8** components           |   0.085 s    |
|                                            |              |
| **SetState** with **1** component          |   0.047 s    |
| **SetState** with **2** component          |   0.047 s    |
| **SetState** with **4** components         |   0.048 s    |
| **SetState** with **8** components         |   0.048 s    |
|                                            |              |
| **Enable** entities with **1** component   |   0.023 s    |
| **Enable** entities with **2** components  |   0.025 s    |
| **Enable** entities with **4** components  |   0.037 s    |
| **Enable** entities with **8** components  |   0.047 s    |
|                                            |              |
| **Disable** entities with **1** component  |   0.019 s    |
| **Disable** entities with **2** components |   0.020 s    |
| **Disable** entities with **4** components |   0.024 s    |
| **Disable** entities with **8** components |   0.032 s    |
|                                            |              |
| **Has** with **1** component type          |   0.007 s    |
| **Has** with **2** component types         |   0.010 s    |
| **Has** with **4** component types         |   0.011 s    |
| **Has** with **8** component types         |   0.011 s    |
|                                            |              |
| **Contains** with **1** component          |   0.006 s    |
| **Contains** with **2** components         |   0.015 s    |
| **Contains** with **4** components         |   0.018 s    |
| **Contains** with **8** components         |   0.025 s    |
|                                            |              |

| 10,000,000 Entities                             | Average Time | Using _State_ - Average Time |
|-------------------------------------------------|:------------:|:----------------------------:|
| **Iterate** entities unpacking **1** component  |   0.010 s    |           0.050 s            |
| **Iterate** entities unpacking **2** components |   0.023 s    |           0.060 s            |
| **Iterate** entities unpacking **3** components |   0.028 s    |           0.124 s            |
| **Iterate** entities unpacking **4** components |   0.033 s    |           0.147 s            |
| **Iterate** entities unpacking **5** components |   0.038 s    |           0.179 s            |
| **Iterate** entities unpacking **6** components |   0.043 s    |           0.206 s            |
|                                                 |              |                              |
| **Stream** entities unpacking **1** component   |   0.025 s    |           0.061 s            |
| **Stream** entities unpacking **2** components  |   0.045 s    |           0.090 s            |
| **Stream** entities unpacking **3** components  |   0.056 s    |           0.124 s            |
| **Stream** entities unpacking **4** components  |   0.066 s    |           0.147 s            |
| **Stream** entities unpacking **5** components  |   0.079 s    |           0.181 s            |
| **Stream** entities unpacking **6** components  |   0.104 s    |           0.209 s            |
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

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
