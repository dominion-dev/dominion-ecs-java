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

## Class Dominion

A Dominion is an independent container for all ECS data. The User Application can create more than one Dominion with
different names. It is the entry point for using the library and provides methods for creating, finding, and deleting
items required by the user application.

| Method                                                                                                         | Description                                                                                       |
|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| static [Dominion](#class-dominion) **create**()                                                                | Creates a new Dominion with an automatically assigned name                                        |
| static [Dominion](#class-dominion) **create**(String name)                                                     | Creates a new Dominion with the provided name                                                     |
| String **getName**()                                                                                           | Returns the Dominion name                                                                         |
| [Entity](#class-entity) **createEntity**(Object... components);                                                | Creates an Entity by adding zero or more POJO components.                                         |
| [Entity](#class-entity) **createEntityAs**([Entity](#class-entity) prefab, Object... components);              | Creates an Entity by using another Entity as prefab and adding zero or more POJO components.      |
| [Entity](#class-entity) **createEntity**(String name, Object... components);                                   | Creates a named Entity by adding zero or more POJO components.                                    |
| [Entity](#class-entity) **createEntityAs**(String name, [Entity](#class-entity) prefab, Object... components); | Creates a named Entity by using another Entity as prefab and adding zero or more POJO components. |
| boolean **deleteEntity**([Entity](#class-entity) entity);                                                      | Delete the  entity by freeing the id and canceling the reference to all components, if any        |
| [Results](#class-results)<Results.Comp1> **findEntitiesWith**(Class\<T> type);                                 | Finds all entities with a component of the specified type                                         |
| [Results](#class-results)<Results.CompN> **findEntitiesWith**(Class\<T1> type1,..)                             | Finds all entities with components of the specified types                                         |

## Class Entity

An Entity identifies a single item and is represented as a unique integer value within a Dominion. Entities can have a
name and contain zero or more components that are POJOs with no behavior. Entities can change components dynamically,
can be disabled and re-enabled and can have a given Enum value to optionally set a state.

| Method                                                  | Description                                                                    |
|---------------------------------------------------------|--------------------------------------------------------------------------------|
| String **getName**()                                    | Returns the entity name                                                        |
| [Entity](#class-entity) **add**(Object... components)   | Adds one or more components that are POJOs with no behavior                    |
| Object **remove**(Object component)                     | Removes a component if present                                                 |
| Object **removeType**(Class\<?> componentType)          | Removes a component if there is a component of the specified type              |
| boolean **has**(Class\<?> componentType)                | Checks if there is a component of the specified type                           |
| boolean **contains**(Object component)                  | Checks if the specified component is present                                   |
| <S extends Enum\<S>> Entity **setState**(S state)       | Sets a state to the entity or remove the current state by passing a null value |
| boolean **isEnabled**()                                 | Checks if the entity is enabled                                                |
| [Entity](#class-entity) **setEnabled**(boolean enabled) | Enable/Disables the entity                                                     |
|                                                         |                                                                                |


## Class Results

Results is the output of the Dominion.findEntitiesWith methods and represents a simple container of all entities that
match a set of components and, optionally, have a specified state. Results can be further filtered by specifying one or 
more component types to exclude. Both iterator and stream methods are available to retrieve found entities in sequence.

## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
