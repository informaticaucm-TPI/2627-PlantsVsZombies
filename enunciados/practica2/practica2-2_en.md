<!-- TOC start -->
- [Assignment 2 (Part II): Plants versus Zombies Extended](#práctica-2-parte-ii-plantas-contra-zombis-extended)
- [Basic extensions to the game](#extensiones-básicas-del-juego)
  * [Introducing new game elements](#incorporación-de-nuevos-objetos-de-juego)
    + [The cherry bomb plant](#cherrybomb-plant)
    + [The wall-nut plant](#wall-nut-plant)
    + [The buckethead zombie](#buckethead-zombie)
    + [The sporty zombie](#sporty-zombie)
    + [The explosive zombie](#explosive-zombie)
  * [Introducing new commands](#commands)
    + [Debugging commands](#debugging-commands)
    + [Reset command](#reset-command)
- [Changing the mechanism for obtaining suncoins](#suncoin-mechanism)
  * [Description](#detalles-de-la-mecánica)
  * [Implementation](#implementación-de-la-mecánica)
- [Implementation details](#detalles-de-implementación)
    + [Implementation of the sun mechanism](#variables-estáticas)
      - [Counting generated suns and caught suns](#soles-generados-y-soles-cogidos)
      - [Ensuring only one catch on each cycle](#catchcommand)
  * [The `GameAction` interface and the `ExplosionAction` class](#la-interfaz-gameactions-y-las-acciones-explosionaction)
  * [Adding generated zombies to the game](#adding-zombies)
- [Tests](#pruebas)
<!-- TOC end -->
<!-- TOC --><a name="práctica-2-parte-ii-plantas-contra-zombis-extended"></a>
# Assignment 2 (Part II): Plants versus Zombies Extended

**Submission (of parts I and II): November 18th, 12:00**

**Objective:** inheritance, polymorphism, abstract classes and interfaces

We start with a **warning**:

> If you break encapsulation, use methods that return lists or make any use of the Java constructs
`instanceof` or `getClass()` (with the exception of the possible use of `getClass()` in `equals`
methods as shown in the slides) you will automatically fail the assignment. The same applies to
the use of any *DIY getClass* (i.e. construction designed to reveal the concrete subclass of `GameObject`)
such as a set of `isX` methods, one for each subclass `X` of `GameObject`, where in the particular
subclass `Y`, the method `isY` returns true and the other methods return false.

**NOTE:** We recommend you read all of the problem statement before you start to implement your solution,
since it explains all the functionality that you are required to implement before providing some indications
about how you should implement it.

<!-- TOC --><a name="extensiones-básicas-del-juego"></a>
# Basic extensions to the game

Having refactored the code in Part I of this assignment in order to make it more
extendable, among other desirable properties, we now add new functionality by extending both of the
inheritance hierarchies introduced in part I, namely, the *command hierarchy* (i.e. the inheritance
hierarchy headed by the `Command` class) and the *game object hierarchy* (i.e. the inheritance
hierarchy headed by the `GameObject` class). That is to say, we introduce new commands and new game
objects, starting with the latter.

But before proceeding, ensure that you update the classes `GamePrinter` and `Messages` that we provide
in the template of this assignment since they have been modified with respect to the versions provided
in the template of the previous assignment.

<!-- TOC --><a name="incorporación-de-nuevos-objetos-de-juego"></a>
## Introducing new game elements

First, if you have not already done so, you should create two subclasses of `GameObject` called `Plant`
and `Zombie` from which all the different zombie and plant classes inherit.

You should then extend the `GameObject` hierarchy to include the following new plants:

- `CherryBomb`
- `WallNut`

and the following new zombies:

- `BucketHead`
- `Sporty`
- `ExplosiveZombie`

The three new zombies inflict the same damage as the common zombie of the previous assignment and
appear with the same probability.

If your refactored code is well-structured, the introduction of these new elements should require
very little modification.

After adding the new plants, the output of the `list` command should be as follows:

```
Available plants:
[S]unflower: cost='20' suncoins, damage='0', endurance='1'
[P]eashooter: cost='50' suncoins, damage='1', endurance='3'
[W]all-[N]ut: cost='50' suncoins, damage='0', endurance='10'
[C]herry-Bomb: cost='50' suncoins, damage='10', endurance='2'
```

and the output of a game in which instances of the new zombies have appeared should
now have the following aspect:

```
Number of cycles: 20
Sun coins: 130
Remaining zombies: 5
           0              1              2              3              4              5              6              7              8       
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  0 |              |              |              |              |              |     Z[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  1 |              |              |              |              |              |              |              |    Bz[08]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  2 |              |              |              |              |              |    Sz[02]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  3 |              |              |              |              |              |    Ez[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```


<!-- TOC --><a name="cherrybomb-plant"></a>
### The cherry bomb plant

Two cycles after planting, this kamikaze plant (to be implemented by a class called
`CherryBomb`) explodes, inflicting *10* points of damage on all neighburing
zombies (i.e. up, down, left, right and the four diagonals, though note that if the cherry bomb is on the edge
of the board, it will have fewer neighbours); it dies in its own explosion. It can also be
eaten by zombies, having resistance *2*. Its cost is *50* suncoins.

> NOTE: A dead cherry-bomb plant cannot explode.

The symbol used in its textual representation is a lower-case `c` which becomes an upper-case `C` in
the cycle immediately before exploding.

<!-- TOC --><a name="wall-nut-plant"></a>
### The wall-nut plant

This plant (to be implemented by a class called `WallNut`) does not damage the zombies and
simply acts as a barrier to them, having resistance *10*.
It costs *50* suncoins. The symbol used in its textual representation is the string `WN`.

<!-- TOC --><a name="buckethead-zombie"></a>
### The buckethead zombie

This zombie (to be implemented by a class called `BucketHeadZombie`) is slower but more resistant than
the common zombie: it moves every *4* cycles and has
resistance *8*.

The symbol used in its textual representation is the string `Bz`.

<!-- TOC --><a name="sporty-zombie"></a>
### The sporty zombie

This zombie (to be implemented by a class called `SportyZombie`) is faster but less resistant
than the common zombie: it moves every cycle and has
resistance *2*

The symbol used in its textual representation is the string `Sz`.

<!-- TOC --><a name="explosive-zombie"></a>
### The explosive zombie

This zombie (to be implemented by a class called `ExplosiveZombie`) has the same velocity and
resistance as a common zombie but if it is killed by a plant, it explodes causing *3* points
of damage to all neighbouring plants.

The symbol used in its textual representation is the string `Ez`.

<!-- TOC --><a name="commands"></a>
## Introducing new commands

<!-- TOC --><a name="debugging-commands"></a>
### Debugging commands: `addZombie`, `listZombie`, `cheatPlant`
We now extend the command hierarchy by adding new commands. You should already have added the
two debugging commands `addZombie` and `listZombie` in the first part of this assignment. With
the new zombies added in the previous section, the output of the latter command should be as
follows:

```
Available zombies:
Common Zombie: speed='2', damage='1', endurance='5'
BucketHead Zombie: speed='4', damage='1', endurance='8'
Sporty Zombie: speed='1', damage='1', endurance='2'
Explosive Zombie: speed='2', damage='1', endurance='5'
```

Here, we add another debugging command, `cheatPlant`, implemented in the `AddPlantCheat`
class, which has the same effect as the `addPlant` command (in particular, like the 
`addZombie` command, the game advances a cycle on using it) except that it does not consume
suncoins.

<!-- TOC --><a name="reset-command"></a>
### Modification of the `reset` command

Also with the aim of facilitating debugging and testing, we modify the rest command so that
on resetting the game, optionally, a new level and a new seed for the pseudo-random behaviour
can be provided. To accomplish
this, the reset command must be able to receive either zero or two parameters, where in the
latter case, the first parameter is the level and the second is the seed. In the two parameter
case, on resetting the game, the level must be changed to that provided and the `Random`
object must be reset using the value provided for the seed.

After adding the new commands introduced below, the output of the help command should be as follows:

```
Available commands:
[a]ddPlant <col>-<row> <plant>: add a plant in position (col, row)
[c]heat[P]lant <col>-<row> <plant>: add a plant in position (col, row) without consuming suncoins
[l]ist: print the list of available plants
list[Z]ombies: print the list of available zombies
[r]eset [<level> <seed>]: start a new game (if level and seed are both provided, they are used to initialize the game)
[h]elp: print this help message
[e]xit: terminate the program
[n]one | "": skip user action for this cycle
[a]dd[Z]ombie <zombie> <col> <row>: add a zombie in position (col, row)
```

<!-- TOC --><a name="suncoin-mechanism"></a>
# Changing the mechanism for obtaining suncoins.

Until now, the sunflowers updated
the number of suncoins automatically after a certain number of cycles. We now modify the mechanism
by which suncoins are obtained,
making it more similar to that of the original game, so that the sunflowers generate suns
that the player has to explicitly catch in order to obtain suncoins. This will involve changes
to multiple parts of the code.

To do so will involve introducing a new class, `Sun`, and a new command to
catch the suns that appear on the board `catch <col> <row>`. The output of the help command
will then be follows:

```
Available commands:
[a]ddPlant <col>-<row> <plant>: add a plant in position (col, row)
[c]heat[P]lant <col>-<row> <plant>: add a plant in position (col, row) without consuming suncoins
[a]dd[Z]ombie <col>-<row> <zombie>: add a zombie in position (col, row)
[l]ist: print the list of available plants
list[Z]ombies: print the list of available zombies
[r]eset [<level> <seed>]: start a new game (if level and seed are both provided, they are used to initialize the game)
[h]elp: print this help message
[e]xit: terminate the program
[n]one | "": skip user action for this cycle
[c]atch <col>-<row>: catch a sun, if posible, in position (col, row)
```

<!-- TOC --><a name="detalles-de-la-mecánica"></a>
## Description

The behaviour of the new suncoins mechanism is as follows:

Generation of suns:

- Every three cycles, each sunflower (i.e. each instance of the `Sunflower` class on the board)
adds a sun in a random position on the board.

- Every five cycles (independently of the existence of sunflowers), a sun is added in a
random position on the board.

- If such a randomly-generated position is already occupied by a plant of a zombie, the
corresponding sun is not added.

- Any number of suns can share the same position.

- Multiple suns in a position are represented as a single sun in the textual representation of
the board printed after each state change of the game. 

Catching of suns:

- Suns are available to be caught for *10* cycles only, after which they disappear from the game.

- The player obtains *10* suncoins for each sun caught; if there are multiple suns in the position
specified in the catch command, all of them are caught by a single use of the command.

- The catch command does not update the game but suns can only be caught once per cycle.

> Note: The implementation of the `GamePrinter` class provided in the new template has been modified
to print only one sun if there are multiple suns in a given position [^1].

[1]: In fact, the implementation also allows suns to share positions with zombies and plants but
we will not use that possibility.

Suns are represented by the string `*[XX]` (where `XX` are the cycles left before that sun will
disappear) so that the printing of the board should now have the following aspect:

```
Number of cycles: 28
Sun coins: 10
Remaining zombies: 2
Generated suns: 18
Caught suns: 0
           0              1              2              3              4              5              6              7              8       
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  0 |  S[01] *[02] |     *[02]    |              |              |              |  *[06]Sz[02] |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  1 |     S[01]    |              |              |              |              |              |              |     *[05]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  2 |              |              |              |              |              |     *[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
  3 |              |     *[08]    |              |              |              |              |     *[08]    |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```

<!-- TOC --><a name="implementación-de-la-mecánica"></a>
## Implementation

Implementing the new suncoins mechanism requires introducing the following changes:

- Create a new class called `Sun`. Recall that objects of this class cannot be in the game for more than
*10* cycles.

- Modify the behaviour of the `Sunflower` class so that it create objects of class `Sun` instead of
updating the number of suncoins.

- Create a class called `SunManager`, similar to the `ZombieManager` class,  which encapsulates the
random generation of suns.

- Modify the`Game` class in order to delegate the random generation of suns to the `SunsManager`.

- Create a new class called `CatchCommand` used to catch suns (max. of once per cycle). Recall that
use of this command does not cause the game to be updated.

- Modify the game information so that, as well as displaying the current number of suncoins, it also
displays the number of generated suns (whether generated by a sunflower or randomly-generated by the
`SunManager`, whether caught or not) and the number of caught suns.


> **NOTE**: When specifying the initial value of the countdown-until-disappear of objects of the
`Sun` class, take into account that immediately after being added (i.e. in the same game cycle), a
sun's countdown will be updated.

<!-- TOC --><a name="detalles-de-implementación"></a>
# Implementation details

In this section we provide some extra implementation details.

## Implementation of the sun mechanism

<!-- TOC --><a name="soles-generados-y-soles-cogidos"></a>
### Counting generated suns and caught suns

A good way to implement the counter of
generated suns and that of caught suns is as static attributes of the `Sun` class,
which can be updated in the `onEnter()` method and
either in the `onExit()` method [^2] or in the method of the `Sun` class that implements a sun being
caught (which should be declared in the `GameItem` interface)[^3]. Remember to
reset these counters when the `reset` command is used.

[^2]: Who should call the `onExit` life-cycle method? A good implementation would be to have a
`removeDead` method in the `container`, executed at the end of the update phase of each cycle, which
traverses the `gameObject` list and calls the
`onExit` method of objects that are no longer alive, before removing them from the list.
Who should call the `onEnter` life-cycle method? It should be called by the code that adds game objects
to the board.

[^3]: If you have not already done so, you could also implement the counter of the number of zombies
on the board via a static attribute of the `Zombie` class in a similar way (instead of storing this
counter in a method of the ZombieManager and having to update it via the game delegating to the
zombie manager on receiving an "I'm dead"-call from a zombie).

<!-- TOC --><a name="catchcommand"></a>
### Ensuring only one `catch` on each cycle

In order to implement the restriction that a `catch` command can only be executed once on each cycle,
we need to store the information as to whether or not a `catch` command has already been executed on
that cycle. We could use a boolean attribute of the `game` for this purpose.

<!-- TOC --><a name="la-interfaz-gameactions-y-las-acciones-explosionaction"></a>
## The `GameAction` interface and the `ExplosionAction` class

A correct implementation of the new game elements `CherryBomb` y `ExplosiveZombie` requires
**explosion chaining**: e.g. a cherry bomb explodes killing a neighbouring explosive zombie,
which then also explodes damaging a neighbouring plant (which may not be a neighbour of the
exploding cherry bomb). This could be implemented as follows?

- In the `onExit` method of the game objects involved, execute any actions that must occur
on dying, in particular, those that damage other game objects.

- At the end of the update phase, first call the `removeDead` method and then repeatedly:
  
  * execute any pending actions,
  * call the `removeDead` method again (which calls the `onExit` method of each dead
    game object).
  
  until there are no more pending actions

A good way of implementing this type of cascading behaviour, which also has the benefit of
imposing an order of execution on the actions involved, is to define an interface called
`GameAction`:

```java
package tp1.pvz.logic.actions;

public interface GameAction {
	void execute(GameWorld game);
}
```

and then define different types of action as classes that implement this interface. Objects of
these action classes can be generated and placed in a queue (stored in an attribute of the
`Game` class), to be executed in LIFO or FIFO order at the appropriate point in the cycle.

In our case, we would like to implement two kinds of explosion action: one, generated by a
cherry bomb, that damages zombies and another, generated by an explosive zombie, that damages
plants. We can implement both of them in a single `ExplosionAction`, distinguishing the two cases
using a simple boolean attribute. When a game element explodes, instead of directly implementing
the explosion, in the appropriate life-cycle method a new `ExplosionAction` object is created
and given to the game to be placed in the action queue. After each removal of dead elements, the
game executes the actions on the action queue, which may create new dead elements.

Though the java libraries contain a class for implementing a simple stack (`java.util.Stack`),
the javadoc of this class recommends that it not be used! Instead it recommends using the
`java.util.Deque` interface to specify a LIFO queue. This interface is also recommended for
specifying a FIFO queue. We will implement a FIFO queue (FIFO or LIFO would be OK) using the
`java.util.ArrayDeque` class in a variable typed by the interface `java.util.Deque`.

Below we provide a skeleton of the code for managing the game actions as part of an `update`
method of the `Game` class.

```java
public class Game implements GameStatus, GameWorld, GameModel {

    // ...
    private Deque<GameAction> actions;

    // ...

    public void reset(Level level, long seed) {
        // ...
        this.actions = new ArrayDeque<>();
    }

    // to be added to the GameWorld interface
    public void pushAction(GameAction gameAction) {
        this.actions.addLast(gameAction);
    }

    public void update() {

        // ...
        
        // Remove dead and execute pending actions
        container.removeDead();
        while (actionsPending()) {
            executePendingActions();
            container.removeDead()
        }

        this.cycle++;

    }

    private void executePendingActions() {
        while (!this.actions.isEmpty()) {
            GameAction action = this.actions.removeFirst();
            action.execute(this);
        }
    }

    private boolean actionsPending() {
        return !this.actions.isEmpty();
    }
    
}
```

<!-- TOC --><a name="adding-zombies"></a>
## Adding generated zombies to the game


<!-- TOC --><a name="pruebas"></a>
# Tests

