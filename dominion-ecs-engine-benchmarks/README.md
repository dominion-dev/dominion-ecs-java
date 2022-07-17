# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion Engine Benchmarks

Below is a list of the most significant benchmarks related to the current implementation of the API. More benchmarks
will come as new API methods are implemented in the Dominion Engine.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                                       | Average Time |
|:---------------------------------------------------------|:------------:|
| **Create** entities with **no** component                |   0.018 s    |
| **Create** entities with **1** component                 |   0.039 s    |
| **Create** entities with **2** components                |   0.051 s    |
| **Create** entities with **4** components                |   0.064 s    |
| **Create** entities with **8** components                |   0.085 s    |
|                                                          |              |
| **Create** prepared entities with **1** component        |   0.020 s    |
| **Create** prepared entities with **2** components       |   0.022 s    |
| **Create** prepared entities with **4** components       |   0.029 s    |
| **Create** prepared entities with **8** components       |   0.039 s    |
|                                                          |              |
| **Delete** entities with **any** component count         |   0.030 s    |
|                                                          |              |
| **Enable/Disable** entities with **any** component count |   0.009 s    |
|                                                          |              |

| 1,000,000 Entities                 | Average Time |
|------------------------------------|:------------:|
| **Add** up to **1** component      |   0.075 s    |
| **Add** up to **2** components     |   0.076 s    |
| **Add** up to **4** components     |   0.074 s    |
| **Add** up to **8** components     |   0.074 s    |
|                                    |              |
| **Remove** from **1** component    |   0.076 s    |
| **Remove** from **2** components   |   0.070 s    |
| **Remove** from **4** components   |   0.070 s    |
| **Remove** from **8** components   |   0.077 s    |
|                                    |              |
| **Modify** with **1** component    |   0.073 s    |
| **Modify** with **2** components   |   0.069 s    |
| **Modify** with **4** components   |   0.075 s    |
| **Modify** with **8** components   |   0.079 s    |
|                                    |              |
| **SetState** with **1** component  |   0.049 s    |
| **SetState** with **2** component  |   0.048 s    |
| **SetState** with **4** components |   0.047 s    |
| **SetState** with **8** components |   0.048 s    |
|                                    |              |
| **Has** with **1** component type  |   0.011 s    |
| **Has** with **2** component types |   0.019 s    |
| **Has** with **4** component types |   0.019 s    |
| **Has** with **8** component types |   0.019 s    |
|                                    |              |
| **Contains** with **1** component  |   0.011 s    |
| **Contains** with **2** components |   0.020 s    |
| **Contains** with **4** components |   0.025 s    |
| **Contains** with **8** components |   0.025 s    |
|                                    |              |

| 10,000,000 Entities from <br/>1 _Composition_ | Average Time | Selecting _State_<br/>Average Time |
|-----------------------------------------------|:------------:|:----------------------------------:|
| **Iterate** entities unpacking **1** comp.    |   0.030 s    |              0.056 s               |
| **Iterate** entities unpacking **2** comp.    |   0.032 s    |              0.069 s               |
| **Iterate** entities unpacking **3** comp.    |   0.033 s    |              0.047 s               |
| **Iterate** entities unpacking **4** comp.    |   0.035 s    |              0.047 s               |
| **Iterate** entities unpacking **5** comp.    |   0.038 s    |              0.049 s               |
| **Iterate** entities unpacking **6** comp.    |   0.040 s    |              0.099 s               |
|                                               |              |                                    |

| 10,000,000 Entities from <br/>MORE _Compositions_<br/> | Average Time | Selecting _State_<br/>Average Time |
|--------------------------------------------------------|:------------:|:----------------------------------:|
| **Iterate** entities unpacking **1** comp.             |   0.047 s    |              0.047 s               |
| **Iterate** entities unpacking **2** comp.             |   0.047 s    |              0.047 s               |
| **Iterate** entities unpacking **3** comp.             |   0.049 s    |              0.050 s               |
| **Iterate** entities unpacking **4** comp.             |   0.053 s    |              0.054 s               |
| **Iterate** entities unpacking **5** comp.             |   0.056 s    |              0.056 s               |
| **Iterate** entities unpacking **6** comp.             |   0.059 s    |              0.105 s               |
|                                                        |              |                                    |

| 10,000,000 Entities from <br/>1 _Composition_ | Average Time | Selecting _State_<br/>Average Time |
|-----------------------------------------------|:------------:|:----------------------------------:|
| **Stream** entities unpacking **1** comp.     |   0.051 s    |              0.048 s               |
| **Stream** entities unpacking **2** comp.     |   0.057 s    |              0.052 s               |
| **Stream** entities unpacking **3** comp.     |   0.065 s    |              0.061 s               |
| **Stream** entities unpacking **4** comp.     |   0.074 s    |              0.066 s               |
| **Stream** entities unpacking **5** comp.     |   0.084 s    |              0.075 s               |
| **Stream** entities unpacking **6** comp.     |   0.108 s    |              0.099 s               |
|                                               |              |                                    |

| 10,000,000 Entities from <br/>MORE _Compositions_<br/> | Average Time | Selecting _State_<br/>Average Time |
|--------------------------------------------------------|:------------:|:----------------------------------:|
| **Stream** entities unpacking **1** comp.              |   0.062 s    |              0.052 s               |
| **Stream** entities unpacking **2** comp.              |   0.066 s    |              0.070 s               |
| **Stream** entities unpacking **3** comp.              |   0.075 s    |              0.075 s               |
| **Stream** entities unpacking **4** comp.              |   0.094 s    |              0.083 s               |
| **Stream** entities unpacking **5** comp.              |   0.109 s    |              0.092 s               |
| **Stream** entities unpacking **6** comp.              |   0.124 s    |              0.106 s               |
|                                                        |              |                                    |

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
