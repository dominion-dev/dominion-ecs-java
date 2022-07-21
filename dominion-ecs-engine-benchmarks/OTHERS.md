# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion VS Others

[Artemis](https://github.com/junkdog/artemis-odb) is a famous Java ECS and is considered the reference by the community.
It already provides a particular benchmark suite that is a bit
cumbersome: [entity-system-benchmarks](https://github.com/junkdog/entity-system-benchmarks)

The Dominion project already provides micro benchmarks to measure actual performance (as other famous ECS
already [do](https://github.com/abeimler/ecs_benchmark)) and now Dominion adds this new suite to provide a clear
performance comparison with Artemis benchmarking the same features in isolation.

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

| 1,000,000 Entities                                       | Average Time |
|:---------------------------------------------------------|:------------:|
| **Create** prepared entities with **1** component        |   0.020 s    |
| **Create** prepared entities with **2** components       |   0.022 s    |
| **Create** prepared entities with **4** components       |   0.029 s    |
| **Create** prepared entities with **8** components       |   0.039 s    |
|                                                          |              |
| **Delete** entities with **any** component count         |   0.030 s    |
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

| 10,000,000 Entities  _                     | Average Time |
|--------------------------------------------|:------------:|
| **Iterate** entities unpacking **1** comp. |   0.030 s    |
| **Iterate** entities unpacking **2** comp. |   0.032 s    |
| **Iterate** entities unpacking **3** comp. |   0.033 s    |
| **Iterate** entities unpacking **4** comp. |   0.035 s    |
| **Iterate** entities unpacking **5** comp. |   0.038 s    |
| **Iterate** entities unpacking **6** comp. |   0.040 s    |
|                                            |              |

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
