## Dominion

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

Dominion is:

- 🚀 **_fast_** : not only is it an insanely fast ECS in the Java scenario, but it can also **compete with ECS solutions
  for C/C++** (see benchmarks)
- 🤏 **_tiny_**: just a high-performance Core Java library with a minimal footprint and no other dependencies
- 🦾 **_easy_**: exposes a clean, self-explanatory API, and this readme alone will be enough to provide a complete usage
  documentation
- 🛠️ **_wip_**: this project is still in early stages and the API is not yet complete and not fully implemented, but
  every part already built comes with unit tests and benchmarks. There are currently no releases yet and first release
  is scheduled for the first quarter of 2022.

### Performance

Designing a high-performance Java library is always a challenge as the speed of code execution could be affected in
several ways. Dominion mitigates Java performance pitfalls by setting a few key points:

- **_not just standard library_**: the Java standard library implements data structures and algorithms designed without
  making any assumption about the data as they are general purpose. Dominion implements some custom data structure and
  algorithms to increase performances and compete with C/C++ libraries.
- **_reduce garbage collection_**: GC could affect overall performances as its activities run concurrently with user
  code and without direct control. To reduce GC activities significantly, Dominion implements pooling systems to reuse
  arrays living in a heap and create off-heap data structures when is it possible.
- **_concurrency design_**: an ECS library must be fast and able to scale running on a multicore CPU. Otherwise, it
  makes little sense. Writing a highly concurrent library in Java requires non-blocking concurrency and using the right
  tools. Dominion implements concurrency by using even the more powerful StampedLock introduced by Java 8.
- **_use Java 17 LTS_**: just by switching to the last JDK 17 you will get a performance boost for free. As already
  tested by [some users](https://www.optaplanner.org/blog/2021/09/15/HowMuchFasterIsJava17.html) the Java 17 is about
  8-9% faster than Java 11. Dominion started using Java 17 from the beginning and all the benchmarks runs with this LTS
  version of Java.