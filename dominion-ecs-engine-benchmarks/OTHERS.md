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
| **Iterate** entities unpacking **1** component |    104.579 ops/s    |    81.437 ops/s    |
| **Iterate** entities unpacking **2** component |    44.330 ops/s     |    36.314 ops/s    |
| **Iterate** entities unpacking **3** component |    36.032 ops/s     |    27.062 ops/s    |
| **Iterate** entities unpacking **4** component |    30.401 ops/s     |    20.480 ops/s    |
| **Iterate** entities unpacking **5** component |    26.466 ops/s     |    15.617 ops/s    |
| **Iterate** entities unpacking **6** component |    23.168 ops/s     |    11.651 ops/s    |
|                                                |                     |                    |

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Create** entities with **1** component  |    38.144 ops/s     |    38.026 ops/s    |
| **Create** entities with **2** components |    35.134 ops/s     |    27.151 ops/s    |
| **Create** entities with **4** components |    26.034 ops/s     |    13.536 ops/s    |
| **Create** entities with **6** components |    19.119 ops/s     |    9.936 ops/s     |
|                                           |                     |                    |
| **Add** up to **1** component             |    19.798 ops/s     |    18.831 ops/s    |
| **Add** up to **2** components            |    18.566 ops/s     |    18.421 ops/s    |
| **Add** up to **4** components            |    14.731 ops/s     |    18.974 ops/s    |
| **Add** up to **6** components            |    12.786 ops/s     |    17.357 ops/s    |
|                                           |                     |                    |
| **Remove** from **1** component           |    20.906 ops/s     |    25.101 ops/s    |
| **Remove** from **2** components          |    19.354 ops/s     |    23.838 ops/s    |
| **Remove** from **4** components          |    17.082 ops/s     |    24.717 ops/s    |
| **Remove** from **6** components          |    14.105 ops/s     |    24.944 ops/s    |
|                                           |                     |                    |
| **Delete** entities with **1** component  |    56.367 ops/s     |    48.193 ops/s    |
| **Delete** entities with **2** component  |    45.702 ops/s     |    40.987 ops/s    |
| **Delete** entities with **4** component  |    40.462 ops/s     |    31.064 ops/s    |
| **Delete** entities with **6** component  |    34.012 ops/s     |    26.456 ops/s    |
|                                           |                     |                    |

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
