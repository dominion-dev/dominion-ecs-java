# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion VS Others

The Dominion project has benchmarks to measure actual performance and now adds this new suite to provide a clear
performance comparison with other ECS libraries to compare the same features in isolation.

ECS frameworks tested:

* [Dominion v0.8.0-EA](https://github.com/dominion-dev/dominion-ecs-java) - This project.
* [Artemis v2.3.0](https://github.com/junkdog/artemis-odb) - Artemis is a famous Java ECS and is considered the
  reference by the Java community. It already provides a proprietary benchmark suite that doesn't test each important
  feature in isolation: [entity-system-benchmarks](https://github.com/junkdog/entity-system-benchmarks)

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                               | Dominion Throughput | Artemis Throughput |
|:-------------------------------------------------|:-------------------:|:------------------:|
| **Create** entities with **1** component         |    50.507 ops/s     |    37.506 ops/s    |
| **Create** entities with **2** components        |    45.427 ops/s     |    23.548 ops/s    |
| **Create** entities with **4** components        |    35.352 ops/s     |    13.390 ops/s    |
| **Create** entities with **8** components        |    23.734 ops/s     |    6.202 ops/s     |
|                                                  |                     |                    |
| **Delete** entities with **any** component count |       0.030 s       |                    |
|                                                  |                     |                    |
| **Add** up to **1** component                    |       0.075 s       |                    |
| **Add** up to **2** components                   |       0.076 s       |                    |
| **Add** up to **4** components                   |       0.074 s       |                    |
| **Add** up to **8** components                   |       0.074 s       |                    |
|                                                  |                     |                    |
| **Remove** from **1** component                  |       0.076 s       |                    |
| **Remove** from **2** components                 |       0.070 s       |                    |
| **Remove** from **4** components                 |       0.070 s       |                    |
| **Remove** from **8** components                 |       0.077 s       |                    |
|                                                  |                     |                    |

| 10,000,000 Entities                            | Average Time |
|------------------------------------------------|:------------:|
| **Iterate** entities unpacking **1** component |   0.030 s    |
| **Iterate** entities unpacking **2** component |   0.032 s    |
| **Iterate** entities unpacking **3** component |   0.033 s    |
| **Iterate** entities unpacking **4** component |   0.035 s    |
| **Iterate** entities unpacking **5** component |   0.038 s    |
| **Iterate** entities unpacking **6** component |   0.040 s    |
|                                                |              |

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
