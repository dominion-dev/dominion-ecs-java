# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion API

Dominion strives to have a minimal API surface to ease the learning curve for users.

With a bunch of well-organized classes, the library is capable of providing all the functionality implemented by the
engine. This documentation will focus on these few interfaces.

As per tradition, Java projects come with a standard Javadoc site for API documentation. Domination breaks tradition by
putting all API references into this README to provide seamless navigation within the repository.

## Package dev.dominion.ecs.api

| Class                       | Description                                                                                                        |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------|
| [Dominion](#class-dominion) | A Dominion is an independent container for all ECS data.                                                           |
| [Entity](#class-entity)     | An Entity identifies a single item and is represented as a unique integer value within a Dominion.                 |
| [Results](#class-results)   | A Results contains the results of selecting entities that match a set of components and, optionally, have a state. |

### Class Dominion

A Dominion is an independent container for all ECS data. The User Application can create more than one Dominion with
different names. It is the entry point for using the library and provides methods for creating, finding, and deleting
items required by the user application.

| Method                                                                           | Description                                                                                               |
|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| static [Dominion](#class-dominion) **create**()                                  | Creates a new Dominion with an automatically assigned name                                                |
| static [Dominion](#class-dominion) **create**(String name)                       | Creates a new Dominion with the provided name                                                             |
| String **getName**()                                                             | Returns the Dominion name                                                                                 |
| [Entity](#class-entity) **createEntity**(Object... components);                  | Creates a new Entity by adding zero or more POJO components.                                              |
| boolean **deleteEntity**([Entity](#class-entity) entity);                        | Delete the  entity by freeing the id and canceling the reference to all components, if any                |
| [Results](#class-results)<Results.Comp1> **findComponents**(Class\<T> type);     | Retrieves the component of the given type by finding all entities that have the component type            |
| [Results](#class-results)<Results.CompN> **findComponents**(Class\<T1> type1,..) | Retrieves all components of the given types by finding all entities that match the set of component types |

### Class Entity

An Entity identifies a single item and is represented as a unique integer value within a Dominion. Entities can contain
zero or more components that are POJOs with no behavior and can change components dynamically. Entities can be disabled
and re-enabled and can use a given Enum to optionally set a state.

### Class Results

A Results contains the results of selecting entities that match a set of components and, optionally, have a state.

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
