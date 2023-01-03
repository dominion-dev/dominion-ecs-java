# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion Examples

Dominion comes with some documented sample apps to help adopt
the [solution](https://github.com/dominion-dev/dominion-ecs-java).

## Hello Dominion

The **HelloDominion** app, already presented in the home, is the example app to break the ice.

[Here](https://github.com/dominion-dev/dominion-ecs-java/blob/main/dominion-ecs-examples/src/main/java/dev/dominion/ecs/examples/hello/HelloDominion.java)
you find the documented source.

With Dominion already built in your local folder, you can run this example with the following commands:

```
java -jar dominion-ecs-examples/target/dominion-ecs-examples-0.9.0-SNAPSHOT.jar
```

## Dark Entities

Here things start to get more interesting.

**DarkEntities** is a simple example app that could inspire a turn-based rogue-like game running on a terminal window.

Shows how to create a basic _dominion_ of entities, components, and systems by creating and moving a camera with a light
to illuminate and explore a random dungeon map. Implements a wise lighting system able to fork subsystems and distribute
on worker threads to take advantage of multiple cores.

<img alt="dark-entities" src="https://raw.githubusercontent.com/dominion-dev/dominion-ecs-java/main/dominion-ecs-examples/dark-entities-01.gif">

The [main class](https://github.com/dominion-dev/dominion-ecs-java/blob/main/dominion-ecs-examples/src/main/java/dev/dominion/ecs/examples/dark/DarkEntities.java)
implementation is straightforward and provides a comment on almost every line. It can be read from top to bottom.

With Dominion already built in your local folder, you can run this example with the following commands:

```
java -cp dominion-ecs-examples/target/dominion-ecs-examples-0.9.0-SNAPSHOT.jar dev.dominion.ecs.examples.dark.DarkEntities
```

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
