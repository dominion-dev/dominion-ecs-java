# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion VS Others

The Dominion project has several benchmarks to measure throughput and also this benchmark suite to provide clear
performance control with other ECS libraries to compare the same features in isolation.

ECS frameworks tested:

* [Dominion v0.8.0-EA](https://github.com/dominion-dev/dominion-ecs-java) - This project.
* [Artemis v2.3.0](https://github.com/junkdog/artemis-odb) - Artemis is the most famous Java ECS and is considered the
  reference by the Java community. It already provides a proprietary benchmark suite that doesn't test each important
  feature in isolation: [entity-system-benchmarks](https://github.com/junkdog/entity-system-benchmarks)

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 10,000,000 Entities                            | Dominion Throughput | Artemis Throughput |
|------------------------------------------------|:-------------------:|:------------------:|
| **Iterate** entities unpacking **1** component |    104.082 ops/s    |    70.131 ops/s    |
| **Iterate** entities unpacking **2** component |    43.999 ops/s     |    35.388 ops/s    |
| **Iterate** entities unpacking **3** component |    35.979 ops/s     |    27.062 ops/s    |
| **Iterate** entities unpacking **4** component |    30.366 ops/s     |    20.480 ops/s    |
| **Iterate** entities unpacking **5** component |    26.466 ops/s     |    15.306 ops/s    |
| **Iterate** entities unpacking **6** component |    23.123 ops/s     |    11.595 ops/s    |
|                                                |                     |                    |

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Create** entities with **1** component  |    38.144 ops/s     |    37.009 ops/s    |
| **Create** entities with **2** components |    32.125 ops/s     |    23.717 ops/s    |
| **Create** entities with **4** components |    26.034 ops/s     |    12.592 ops/s    |
| **Create** entities with **6** components |    19.119 ops/s     |    9.020 ops/s     |
|                                           |                     |                    |
| **Add** up to **1** component             |    19.432 ops/s     |    17.450 ops/s    |
| **Add** up to **2** components            |    17.843 ops/s     |    17.692 ops/s    |
| **Add** up to **4** components            |    13.896 ops/s     |    16.101 ops/s    |
| **Add** up to **6** components            |    11.959 ops/s     |    15.778 ops/s    |
|                                           |                     |                    |
| **Remove** from **1** component           |    20.708 ops/s     |    22.791 ops/s    |
| **Remove** from **2** components          |    17.518 ops/s     |    22.838 ops/s    |
| **Remove** from **4** components          |    14.832 ops/s     |    22.451 ops/s    |
| **Remove** from **6** components          |    12.907 ops/s     |    21.920 ops/s    |
|                                           |                     |                    |
| **Delete** entities with **1** component  |    55.367 ops/s     |    46.641 ops/s    |
| **Delete** entities with **2** component  |    44.740 ops/s     |    39.685 ops/s    |
| **Delete** entities with **4** component  |    34.187 ops/s     |    29.393 ops/s    |
| **Delete** entities with **6** component  |    21.613 ops/s     |    25.729 ops/s    |
|                                           |                     |                    |

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
