## Dominion

Dominion is an [Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) library for Java

Dominion is:

- 🚀 **_fast_** : not only is it an insanely fast ECS in the Java scenario, but it can also **compete with ECS solutions for
  C/C++** (see benchmarks)
- 🤏 **_tiny_**: just a high-performance Core Java library with a minimal footprint and no other dependencies
- 🦾 **_easy_**: exposes a clean, self-explanatory API, and this readme alone will be enough to provide a complete usage
  documentation
- 🛠️ **_wip_**: this project started small and fast; the API is not yet complete and not fully implemented, but
  every part already built comes with unit tests and benchmarks

### Performance

Designing a high-performance Java library is always a challenge as performance could be affected in several ways:

- _using the standard library_: the Java standard library implements data structures and algorithms designed without
  making any assumption about the data as they are general purpose. Dominion implements some custom data structure and
  algorithms to increase performances and compete with C/C++ libraries.
- _garbage collection_: GC could affect overall performances as its activities run concurrently with user code and
  without direct control. To reduce GC activities significantly, Dominion implements pooling systems to reuse arrays
  living in a heap and create off-heap data structures when is it possible.
- _concurrency design_: an ECS library must be fast and able to scale running on a multicore CPU. Otherwise, it makes
  little sense. Writing a highly concurrent library in Java requires non-blocking concurrency and using the right tools.
  Dominion also implements concurrency by using the more powerful StampedLock introduced by Java 8.