# <img src="https://raw.githubusercontent.com/dominion-dev/dominion-dev.github.io/main/dominion-logo-square.png" align="right" width="100"> Dominion API

Dominion strives to have a minimal API surface to ease the learning curve for users.

With a bunch of well-organized classes, the library is capable of providing all the functionality implemented by the
engine. This documentation will focus on these few interfaces.

As per tradition, Java projects come with a standard Javadoc site for API documentation. Dominion breaks tradition by
putting all API references into this README to provide seamless navigation within the repository.

## Package dev.dominion.ecs.api

| Class                             | Description                                                                                                          |
|-----------------------------------|----------------------------------------------------------------------------------------------------------------------|
| [Dominion](#class-dominion)       | A **Dominion** is an independent container for all ECS data.                                                         |
| [Composition](#class-composition) | A **Composition** is the aggregation of components of which an entity can be made of.                                |
| [Entity](#class-entity)           | An **Entity** identifies a single item and is represented as a unique integer value within a Dominion.               |
| [Results](#class-results)         | A **Results** is a container of all entities that match a set of components and, optionally, have a specified state. |
| [Scheduler](#class-scheduler)     | A **Scheduler** provides methods to submit/suspend/resume systems that are executed on every tick.                   |

## Class Dominion

A **Dominion** is an independent container for all ECS data. The User Application can create more than one Dominion with
different names. It is the entry point for using the library and provides methods for creating, finding, and deleting
items required by the user application.

| Method                                                                                                         | Description                                                                                       |
|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| static [Dominion](#class-dominion) **create**()                                                                | Creates a new Dominion with an automatically assigned name.                                       |
| static [Dominion](#class-dominion) **create**(String name)                                                     | Creates a new Dominion with the provided name.                                                    |
| String **getName**()                                                                                           | Returns the Dominion name.                                                                        |
| [Entity](#class-entity) **createEntity**(Object... components);                                                | Creates an Entity by adding zero or more POJO components.                                         |
| [Entity](#class-entity) **createEntityAs**([Entity](#class-entity) prefab, Object... components);              | Creates an Entity by using another Entity as prefab and adding zero or more POJO components.      |
| [Entity](#class-entity) **createEntity**(String name, Object... components);                                   | Creates a named Entity by adding zero or more POJO components.                                    |
| [Entity](#class-entity) **createEntityAs**(String name, [Entity](#class-entity) prefab, Object... components); | Creates a named Entity by using another Entity as prefab and adding zero or more POJO components. |
| boolean **deleteEntity**([Entity](#class-entity) entity);                                                      | Delete the  entity by freeing the id and canceling the reference to all components, if any        |
| [Results](#class-results)<EntityWith1> **findEntitiesWith**(Class\<T> type);                                   | Finds all entities with a component of the specified type.                                        |
| [Results](#class-results)<EntityWithN> **findEntitiesWith**(Class\<T1> type1,..)                               | Finds all entities with components of the specified types.                                        |
| [Results](#class-results)<With1> **findCompositionsWith**(Class\<T> type);                                     | Finds all compositions with a component of the specified type.                                    |
| [Results](#class-results)<WithN> **findCompositionsWith**(Class\<T1> type1,..)                                 | Finds all compositions with components of the specified types.                                    |

## Class Composition

A Composition is the aggregation of components of which an entity can be made of.
Provides methods to prepare for entity creation with a certain component type composition or to prepare for entity
change by specifying which component type is to be added and / or removed.

<pre>
    Dominion dominion = Dominion.create();
    Composition composition = dominion.composition();

    // prepared entity creations
    var compositionOf1 = composition.of(Comp.class);
    Entity entity1 = dominion.createPreparedEntity(compositionOf1.withValue(new Comp(0)));
    Entity entity2 = dominion.createPreparedEntity(compositionOf1.withValue(new Comp(1)));

    // prepared entity changes
    var modifyAdding1 = composition.byAdding1AndRemoving(Comp2.class);
    dominion.modifyEntity(modifyAdding1.withValue(entity1, new Comp2()));
    dominion.modifyEntity(modifyAdding1.withValue(entity2, new Comp2()));
</pre>

| Method                                                                                                                                          | Description                                                                                               |
|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Of1\<T> **of**(Class\<T> type);                                                                                                                 | Provides a prepared composition of a single component type.                                               |
| OfN\<T1,..,TN> **ofN**(Class\<T1> type1, .., Class\<TN> typeN);                                                                                 | Provides a prepared composition of N component types.                                                     |
| ByRemoving **byRemoving**(Class<?>... removedCompTypes);                                                                                        | Provides a prepared modifier to remove one or more component types.                                       |
| ByAdding1AndRemoving\<T> **byAdding1AndRemoving**(Class<T> addedCompType, Class<?>... removedCompTypes);                                        | Provides a prepared modifier to add one component type and optionally remove one or more component types. |
| ByAddingNAndRemoving\<T1,..,TN> **byAddingNAndRemoving**(Class<T1> addedCompType1, .., Class<TN> addedCompTypeN, Class<?>... removedCompTypes); | Provides a prepared modifier to add N component types and optionally remove one or more component types.  |


## Class Entity

An **Entity** identifies a single item and is represented as a unique integer value within a Dominion. Entities can have 
a name and contain zero or more components that are POJOs with no behavior. Entities can change components dynamically,
can be disabled and re-enabled and can have a given Enum value to optionally set a state.

| Method                                                  | Description                                                                     |
|---------------------------------------------------------|---------------------------------------------------------------------------------|
| String **getName**()                                    | Returns the entity name.                                                        |
| [Entity](#class-entity) **add**(Object component)       | Adds one component that is a POJO  with no behavior.                            |
| boolean **remove**(Object component)                    | Removes a component if present.                                                 |
| boolean **removeType**(Class\<?> componentType)         | Removes a component if there is a component of the specified type.              |
| boolean **has**(Class\<?> componentType)                | Checks if there is a component of the specified type.                           |
| boolean **contains**(Object component)                  | Checks if the specified component is present.                                   |
| <S extends Enum\<S>> Entity **setState**(S state)       | Sets a state to the entity or remove the current state by passing a null value. |
| boolean **isEnabled**()                                 | Checks if the entity is enabled.                                                |
| [Entity](#class-entity) **setEnabled**(boolean enabled) | Enable/Disables the entity.                                                     |


## Class Results

A **Results** instance is the output of the [Dominion](#class-dominion)::**findCompositionWith** and 
::**findEntitiesWith** methods and represents a simple container of all compositions that match a set of components and,
optionally, the related entities.
Results can be filtered by specifying one or more component types to include or exclude in the select list.
Both iterator and stream methods are available to retrieve the results.

| Method                                                                   | Description                                                                            |
|--------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| Iterator\<T> **iterator**();                                             | Provides an iterator to retrieve found entities in sequence.                           |
| Stream\<T> **stream**();                                                 | Creates a sequential stream to supports functional-style operations on found entities. |
| [Results\<T>](#class-results) **without**(Class\<?>... componentTypes);  | Provides a filtered Results without one or more component types to exclude.            |
| [Results\<T>](#class-results) **withAlso**(Class\<?>... componentTypes); | Provides a Results also considering one or more types of components as a filter.       |
| [Results\<T>](#class-results) **withState**(S state);                    | Provides a filtered Results with only entities having the required state.              |


## Class Scheduler

A **Scheduler** provides methods to submit/suspend/resume systems that are executed on every tick.
Systems are defined as a plain old Java Runnable type, so they can be provided as lambda expressions and are
guaranteed to run sequentially.
Parallel systems run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.
Systems, even if running parallel, can be suspended and resumed at any time, maintaining the order of execution.
A system can fork by creating several subsystems for immediate parallel executions and "join" while waiting for
all subsystems to execute.
Schedulers can start a periodic tick that becomes enabled immediately and subsequently with the given fixed rate.
A deltaTime method provides the time in seconds between the last tick and the current tick.
<pre>
schedule(A)
parallelSchedule(B,C)
schedule(D)
tickAtFixedRate(1)

system A ---#---------------|*------|*------|*------|*----------
system B --------#----------|-*-----|-*-----|-*-----|-*---------
system C --------#----------|-*-----|-*-----|-*-----|-*---------
system D -------------#-----|--*----|--*----|--*----|--*--------
            |    |    |    tick0s  tick1s  tick2s  tickNs
           +A   +B,C  +D    |>

suspend(B)
suspend(D)

system A -|*--------------|*--------------|*-------
system B -|-*----X--------|---------------|--------
system C -|-*-------------|-*-------------|-*------
system D -|--*-------X----|---------------|--------
         tickNs  |   |   tickN+1s      tickN+2s
                -B  -D

resume(B)

system A -|*----------|*-----------|*-------
system B -|------#----|-*----------|-*------
system C -|-*---------|-*----------|-*------
system D -|---------- |------------|--------
         tickNs  |   tickN+1s     tickN+2s
                +B

systemA.forkAndJoinAll(subsystemA1, subsystemA2)

system A -|*_*--------|*_*--------|*_*--------|
 sub A1  -|-*---------|-*---------|-*---------|
 sub A2  -|-*---------|-*---------|-*---------|
system B -|---*-------|---*-------|---*-------|
system C -|---*-------|---*-------|---*-------|
            |           |           |
            *A1,A2     *A1,A2      *A1,A2

</pre>

| Method                                                | Description                                                                                                                                                                                         |
|-------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Runnable **schedule**(Runnable system);               | Submits a system that becomes enabled immediately and executed on every tick. Scheduled systems are guaranteed to execute sequentially, and no more than one task will be active at any given time. |
| Runnable[] **parallelSchedule**(Runnable... systems); | Submits systems that become enabled immediately and executed on every tick. Parallel systems run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.              |
| void **suspend**(Runnable system);                    | Suspends an already scheduled system preserving its execution order.                                                                                                                                |
| void **resume**(Runnable system);                     | Resumes an already suspended system in the original execution order.                                                                                                                                |
| void **forkAndJoin**(Runnable subsystem);             | A system can fork by creating a subsystem for immediate execution and "join" while waiting for the subsystem to execute.                                                                            |
| void **forkAndJoinAll**(Runnable... subsystems);      | A system can fork by creating several subsystems for immediate parallel executions and "join" while waiting for all subsystems to execute.                                                          |
| void **tick**();                                      | Starts running all scheduled systems sequentially in a guaranteed order. Systems sent in parallel run concurrently in the same slot, which is scheduled sequentially in a guaranteed order.         |
| void **tickAtFixedRate**(int ticksPerSecond);         | Starts a periodic tick that becomes enabled immediately and subsequently with the given fixed rate.                                                                                                 |
| double **deltaTime**();                               | DeltaTime is the time in seconds between the last tick and the current tick.                                                                                                                        |
| boolean **shutDown**();                               | Initiates an orderly shutdown in which previously submitted systems are executed, but no new systems will be accepted.                                                                              |


## Support Dominion

If you want to support Dominion project, consider giving it a **Star** ⭐️
