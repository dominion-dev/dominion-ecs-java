# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion VS Others

The Dominion project provides several benchmarks of all its features, but this suite provides a clear comparison to
other ECS libraries to see some of the same features in isolation.

ECS frameworks tested:

* [Dominion v0.8.0-EA](https://github.com/dominion-dev/dominion-ecs-java) - This project.
* [Artemis v2.3.0](https://github.com/junkdog/artemis-odb) - Artemis is a popular Java ECS and is considered the
  reference by the Java community. It already provides a proprietary benchmark suite, but it doesn't test each important
  feature in isolation, see: [entity-system-benchmarks](https://github.com/junkdog/entity-system-benchmarks)

These single-threaded benchmarks were run on a notebook, a MacBook Pro 2021 with M1 Pro 10 core, compiled and executed
natively with JDK Azul-17 aarch64:

---

| 10,000,000 Entities                             | Dominion Throughput | Artemis Throughput |
|-------------------------------------------------|:-------------------:|:------------------:|
| **Iterate** entities unpacking **1** component  |    141.251 ops/s    |    81.437 ops/s    |
| **Iterate** entities unpacking **2** components |    53.901 ops/s     |    36.314 ops/s    |
| **Iterate** entities unpacking **3** components |    42.043 ops/s     |    27.062 ops/s    |
| **Iterate** entities unpacking **4** components |    34.070 ops/s     |    20.480 ops/s    |
| **Iterate** entities unpacking **5** components |    29.024 ops/s     |    15.617 ops/s    |
| **Iterate** entities unpacking **6** components |    25.486 ops/s     |    11.651 ops/s    |

<img alt="iterate" src="https://chart.googleapis.com/chart?cht=bvg&chof=png&chs=600x300&chbh=25,3,20&chdl=Dominion|Artemis&chco=3333FF,8888FF&chxt=y,x&chg=16.66,0&chma=50&chtt=Iterate+10M+Entities+-+throughput+ops/s&chl=1+comp|2+comp|3+comp|4+comp|5+comp|6+comp&chds=0,150&chxr=0,0,150&chd=t:141.251,53.901,42.043,34.070,29.024,25.486|81.437,36.314,27.062,20.480,15.617,11.651">

---

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Create** entities with **1** component  |    52.465 ops/s     |    38.026 ops/s    |
| **Create** entities with **2** components |    39.441 ops/s     |    27.151 ops/s    |
| **Create** entities with **4** components |    28.770 ops/s     |    13.536 ops/s    |
| **Create** entities with **6** components |    20.370 ops/s     |    9.936 ops/s     |

<img alt="create" src="https://chart.googleapis.com/chart?cht=bvg&chof=png&chs=600x300&chbh=25,3,30&chdl=Dominion|Artemis&chco=3333FF,8888FF&chxt=y,x&chg=25,0&chma=50&chtt=Create+1M+Entities+-+throughput+ops/s&chl=1+comp|2+comp|4+comp|6+comp&chds=0,55&chxr=0,0,55&chd=t:52.465,39.441,28.770,20.370|38.026,27.151,13.536,9.936">

---

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Add** up to **1** component             |    19.249 ops/s     |    18.831 ops/s    |
| **Add** up to **2** components            |    17.321 ops/s     |    18.421 ops/s    |
| **Add** up to **4** components            |    14.818 ops/s     |    18.974 ops/s    |
| **Add** up to **6** components            |    13.123 ops/s     |    17.357 ops/s    |

<img alt="add" src="https://chart.googleapis.com/chart?cht=bvg&chof=png&chs=600x300&chbh=25,3,30&chdl=Dominion|Artemis&chco=3333FF,8888FF&chxt=y,x&chg=25,0&chma=50&chtt=Add+Component+to+1M+Entities+-+throughput+ops/s&chl=1+comp|2+comp|4+comp|6+comp&chds=0,20&chxr=0,0,20&chd=t:19.249,17.321,14.818,13.123|18.831,18.421,18.974,17.357">

---

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Remove** from **1** component           |    19.095 ops/s     |    25.101 ops/s    |
| **Remove** from **2** components          |    17.680 ops/s     |    23.838 ops/s    |
| **Remove** from **4** components          |    14.762 ops/s     |    24.717 ops/s    |
| **Remove** from **6** components          |    12.708 ops/s     |    24.944 ops/s    |

<img alt="remove" src="https://chart.googleapis.com/chart?cht=bvg&chof=png&chs=600x300&chbh=25,3,30&chdl=Dominion|Artemis&chco=3333FF,8888FF&chxt=y,x&chg=25,0&chma=50&chtt=Remove+Component+from+1M+Entities+-+throughput+ops/s&chl=1+comp|2+comp|4+comp|6+comp&chds=0,30&chxr=0,0,30&chd=t:19.095,17.680,14.762,12.708|25.101,23.838,24.717,24.944">

---

| 1,000,000 Entities                        | Dominion Throughput | Artemis Throughput |
|:------------------------------------------|:-------------------:|:------------------:|
| **Delete** entities with **1** component  |    47.474 ops/s     |    48.193 ops/s    |
| **Delete** entities with **2** component  |    47.052 ops/s     |    40.987 ops/s    |
| **Delete** entities with **4** component  |    46.718 ops/s     |    31.064 ops/s    |
| **Delete** entities with **6** component  |    31.415 ops/s     |    26.456 ops/s    |

<img alt="delete" src="https://chart.googleapis.com/chart?cht=bvg&chof=png&chs=600x300&chbh=25,3,30&chdl=Dominion|Artemis&chco=3333FF,8888FF&chxt=y,x&chg=25,0&chma=50&chtt=Delete+1M+Entities+-+throughput+ops/s&chl=1+comp|2+comp|4+comp|6+comp&chds=0,50&chxr=0,0,50&chd=t:47.474,47.052,46.718,31.415|48.193,40.987,31.064,26.456">

---

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
