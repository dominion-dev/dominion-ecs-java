# <img src="https://github.com/dominion-dev/dominion-ecs-java/raw/main/dominion-logo-square.png" align="right" width="100"> Dominion API

Dominion strives to have a minimal API surface to ease the learning curve for users.

With a bunch of well-organized interfaces, the library is capable of providing all the functionality implemented by the
engine. This documentation will focus on these few interfaces.

As per tradition, Java projects come with a standard Javadoc site for API documentation. Domination breaks tradition by
putting all API references into this README to provide seamless navigation within the repository.

## Package dev.dominion.ecs.api

| Class                       | Description                                                                                         |
|-----------------------------|-----------------------------------------------------------------------------------------------------|
| [Dominion](#class-dominion) | An independent container for all ECS data.                                                          |
| [Entity](#class-entity)     | The entity identifies a single item and is represented as a unique integer value within a Dominion. |
| [Results](#class-results)   | The results of selecting entities that have a certain set of components and, optionally, a state.   |

### Class Dominion

An independent container for all ECS data. The User Application can create more than one _Dominion_ with different
names. It is the entry point for using the library and provides methods for creating, finding, and deleting items
required by the user application.

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

The entity identifies a single item and is represented as a unique integer value within a Dominion.

### Class Results

The results of selecting entities that have a certain set of components and, optionally, a state.

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
