# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion Engine Benchmarks

Below is a list of the most significant benchmarks related to the current implementation of the API. More benchmarks
will come as new API methods are implemented in the Dominion Engine.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                                  | Average Time |
|:----------------------------------------------------|:------------:|
| **Create** entities with **no** component           |   0.018 s    |
| **Create** entities with **1** component            |   0.039 s    |
| **Create** entities with **2** components           |   0.051 s    |
| **Create** entities with **4** components           |   0.064 s    |
| **Create** entities with **8** components           |   0.085 s    |
| **Create** entities with **16** components          |   0.124 s    |
|                                                     |              |
| **Delete** entities with **any** comp.count         |   0.030 s    |
|                                                     |              |
| **Enable/Disable** entities with **any** comp.count |   0.009 s    |
|                                                     |              |

| 1,000,000 Entities                  | Average Time |
|-------------------------------------|:------------:|
| **Add** to **1** component          |   0.096 s    |
| **Add** to **2** components         |   0.102 s    |
| **Add** to **4** components         |   0.112 s    |
| **Add** to **8** components         |   0.150 s    |
| **Add** to **16** components        |   0.220 s    |
|                                     |              |
| **Remove** from **1** component     |   0.046 s    |
| **Remove** from **2** components    |   0.085 s    |
| **Remove** from **4** components    |   0.113 s    |
| **Remove** from **8** components    |   0.140 s    |
| **Remove** from **16** components   |   0.216 s    |
|                                     |              ||                                        |              |
| **SetState** with **1** component   |   0.050 s    |
| **SetState** with **4** components  |   0.050 s    |
| **SetState** with **8** components  |   0.050 s    |
| **SetState** with **16** components |   0.050 s    |
|                                     |              |
| **Has** with **1** component type   |   0.009 s    |
| **Has** with **4** component types  |   0.016 s    |
| **Has** with **8** component types  |   0.016 s    |
| **Has** with **16** component types |   0.016 s    |
|                                     |              |
| **Contains** with **1** component   |   0.009 s    |
| **Contains** with **4** components  |   0.019 s    |
| **Contains** with **8** components  |   0.019 s    |
| **Contains** with **16** components |   0.019 s    |
|                                     |              |

| 10,000,000 Entities from <br/>1 _Composition_ | Average Time | Selecting _State_<br/>Average Time |
|-----------------------------------------------|:------------:|:----------------------------------:|
| **Iterate** entities unpacking **1** comp.    |   0.030 s    |              0.056 s               |
| **Iterate** entities unpacking **2** comp.    |   0.032 s    |              0.069 s               |
| **Iterate** entities unpacking **3** comp.    |   0.033 s    |              0.047 s               |
| **Iterate** entities unpacking **4** comp.    |   0.035 s    |              0.047 s               |
| **Iterate** entities unpacking **5** comp.    |   0.038 s    |              0.049 s               |
| **Iterate** entities unpacking **6** comp.    |   0.051 s    |              0.099 s               |
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
| **Stream** entities unpacking **1** comp.     |   0.051 s    |              0.049 s               |
| **Stream** entities unpacking **2** comp.     |   0.057 s    |              0.053 s               |
| **Stream** entities unpacking **3** comp.     |   0.065 s    |              0.063 s               |
| **Stream** entities unpacking **4** comp.     |   0.074 s    |              0.069 s               |
| **Stream** entities unpacking **5** comp.     |   0.084 s    |              0.078 s               |
| **Stream** entities unpacking **6** comp.     |   0.108 s    |              0.103 s               |
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
