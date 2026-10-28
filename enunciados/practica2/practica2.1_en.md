<!-- TOC start -->
- [Práctica 2 - Parte I: Plants vs Zombies Refactored](#assignment-2---part-i-pvz-refactored)
	- [Introduction](#introduction)
		- [The Model-View-Controller (MVC) architectural pattern](#mvc-pattern)
		- [Overview of the assignment](#overview)
	- [Refactoring the solution of the previous assignment](#refactoring-the-solution-of-the-previous-assignment)
		- [The command inheritance hierarchy](#command-pattern)
		- [Using interfaces to define different projections of the `Game` class](#projections-of-the-game-class)
		- [The game-object inheritance hierarchy](#game-object-inheritance-hierarchy)
		- [Delegating functionality of the `game` to the `container`](#game-object-container)
		- [Game object factories](#game-object-factories)
		- [The `GameItem` interface and callbacks](#game-item-interface)
		- [Generalising the interactions between game objects](#generalising-interactions)
	- [Testing](#pruebas)
<!-- TOC end -->

<!-- TOC --><a name="assignment-2---part-i-pvz-refactored"></a>
# Assignment 2 - Part I: Plants vs Zombies Refactored

**Submission (of parts I and II): November 16th, 09:00**

**Objetivos:** Use of inheritance, polymorphism, abstract classes and interfaces.

<!--
**Preguntas frecuentes**: Como es habitual (y normal), que tengáis dudas, las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2324-SpaceInvaders-SOLUCION/commits/main/enunciados/faq.md).
-->

<!-- TOC --><a name="introduction"></a>
## Introduction

<!-- TOC --><a name="mvc-pattern"></a>
### The Model-View-Controller (MVC) architectural pattern
You may have noticed that the package structure of the code provided in the previous assignment conforms to the Model-View-Controller (MVC) architectural pattern:

+ The **Model**  component of an MVC application contains the logic. In our case, the *Model* contains the rules of the game, the handling of the different elements of the game and, in general, everything concerned with the game such as remaining cycles, whether or not the game has finished, whether or not the player has won, etc. Correspondingly, in our package structure, the `logic` package contains the `Game` class and other classes. The `Game` class contains a method `update` to update the state of the game and methods that are called by the `run` method of the `Controller` class (see below) when the corresponding command is entered, such as the `reset` method.

+ The **View** component of an MVC application manages the user interface or user interfaces (e.g. typically, the interface presented to an administrator is different to that presented to a user). In our case, the *View* component only has to manage a simple console display. Correspondingly, in our package structure, the `view` package contains the `GamePrinter` class responsible for displaying the state of the game on the console. The constructor of `GamePrinter` receives an instance of the `Game` class. The `GamePrinter` class implements the `GameView` interface. 

+ The **Controller** component of an MVC application recieves the user input, parses it and passes the resulting data to the appropriate part of the *Model* for processing before telling the *View* to display the results. In our case, the *Controller* handles only simple commands rather than, for example, capturing keyboard presses or receiving network requests. Correspondingly, in our package structure, the `control` package contains the `Controller` class whose `run` method contains the main loop of the game: while the game has not finished, call the implementation of the appropriate method of the `GameView` interface to display the state of the game, then prompt the player to enter a command, then call the appropriate method of the `Game` class to process the chosen command. Notice that the `Controller` class should **not** contain any of the game logic, since the logic belongs in the *Model*.

<!-- TOC --><a name="overview"></a>
### Overview of this assignment

In this assignment, we apply the mechanisms that OOP offers to improve and extend
the code developed in the previous assignment as follows:

- In Part I of this assignment, we refactor[^1] the code of the previous assignment in the following
ways (involving two inheritance hierarchies):

  - Firstly, by removing some code from the controller `run` method and distributing
    its functionality among an inheritance hierarchy of classes, enabling the `Controller`
    class to be generic, i.e. it has no dependence on the exact commands used.

  - Secondly, by defining an inheritance hierarchy of game objects, the leaves of which will
    be the classes that
    were used in the previous assignment, namely `Peashooter`, `Sunflower` and `CommonZombie`,
    enabling the `Game` class and the `GameObjectContainer` class defined below to be generic,
    i.e. to have no
    dependence on the exact game objects used (**they certainly do not use `isinstance`
    or `getClass`!**)
    The use of inheritance enables us to avoid having identical, or nearly identical, code
    in different classes. It also enables us to reorganise how we store the information
    about the state of the game, using a single data structure instead of multiple lists.
    
    To preserve the genericity of the `Game` and `GameObjectContainer` classes without
    introducing *ad-hoc* properties in the game objects we will introduce a generic
    mechanism for handling interactions between game objects using a technique called
    *double-dispatch*.
    
  - Thirdly, we add interfaces to be implemented by the `Game` class which provide three
    partial views of its public methods, thereby classifying its client classes into
    three groups (classes in the *View*, classes in the *Controller* and classes internal
    to the *Model*) and ensuring that each group can only invoke the subset of
    the `Game` class methods contained in its partial view.
    
  The aim of these changes is to produce code that is more extensible, adaptable, maintainable,
  robust, etc. without changing the functionality. Note that if the functionality has not changed,
  the refactored code should pass the same tests as the code submitted for Assignment 1.

- Once the code has been refactored, in part II of this assignment, we add new commands and new
  elements to the game, thereby demonstrating how the new structure facilitates these extensions.

In summary, the objective of the first part of Assignment 2 is to make the code more extensible,
adaptable, maintainable, robust, etc. without changing its functionality.

[^1]: Refactoring means changing the structure of code (to improve it, presumably) without changing
its functionality.

<!-- TOC --><a name="refactoring-the-solution-of-the-previous-assignment"></a>
## Refactoring the solution to the previous assignment

<!-- TOC --><a name="command-pattern"></a>
### The command inheritance hierarchy

In the application built in the previous assignment, the user could enter several different commands in
order to update the game,
reset the game, ask for help, etc. The objective of the first half of the refactoring is to introduce
a structure to the part of the code that is concerned with processing the user commands which
will facilitate the addition of new commands, i.e. which will enable new commands to be added
with minimal modifications to the existing code. This structure is the well-known software
design pattern referred to as the *command pattern*[^2].

[^2]: As one would expect, this is a very well-known problem
in OOP which has solutions that are very well tried and tested, so we do not need to invent our own.
The solution we will use is a variant of the *Command* design pattern, one of the twenty-three
software patterns presented in the foundational software patterns book "Design Patterns: Elements of
Reusable Object-Oriented Software" first published in 1994. The general idea of the command
pattern is to encapsulate each command in its own class. You will study software design patterns in
general, and this software design pattern in particular, in the Software Engineering course.

The following classes are involved in our application of the *command pattern*:

-  `Command`: an interface that defines the public methods of any command, namely `execute`, `parse` and
   `helpText`.

- `AbstractCommmand`: an abstract class that encapsulates the functionality common to all the concrete
   commands. It has four attributes of type `String` that are initialised in its constructor -- `name`,
   `shortcut` (the abbreviated name), `details` (the initial part of its help message), `help`
   (the details of its help message) -- and *getter* methods for at least `name` and `shortcut` whose
   values are needed by the `parse` methods of the subclasses (instead of making these attributes
   `protected`).

- `NoParamsCommand`: an abstract class that inherits from `AbstractCommmand` and which is the superclass of all
   those concrete command classes that represent a command with no parameters, such as `HelpCommand`.
   The *raison d'être* of this class is that the parsing of all commands with no parameters only differs
   in the value of the `name` and `shortcut` attributes used to compare with the input text, so that the
   `parse` method of all the corresponding classes can be inherited from the `NoParamsCommand` class.
   Evidently, the same is not true of the `execute` method of the classes representing commands with no
   parameters, which will be different for every command, for which reason, the `NoParamsCommand` class
   must be abstract.

- Concrete command classes: `HelpCommand`, `ExitCommand`, etc., one such class for each program command.
  In the case of a command with parameters, the corresponding class has attributes to store the value
  of these parameters. Each concrete command class also contains the four attributes `name`, `shortcut`,
  `details` and `help` of the `AbstractCommand` class (but cannot access them directly since they are
  `private` to the `AbstractCommand` class). Though these attributes are assigned values in the
  constructor of the `AbstractCommand` class, the values used are specific to each concrete command class.
  This is achieved by passing these values up from the constructor of each concrete command class, in
  which they are defined as constants. For example, the code of the `HelpCommand` class has the following
  aspect:
    
  ```java
  public class HelpCommand extends NoParamsCommand {

	private static final String NAME = Messages.COMMAND_HELP_NAME;
	private static final String SHORTCUT = Messages.COMMAND_HELP_SHORTCUT;
	private static final String DETAILS = Messages.COMMAND_HELP_DETAILS;
	private static final String HELP = Messages.COMMAND_HELP_HELP;

	public HelpCommand(){
	    super(NAME, SHORTCUT, DETAILS, HELP);
	}

    // Implementation of the execute method
  }
  ```
  where the corresponding constants of the `Messages` class have the following values:
  ```java
  public static final String COMMAND_HELP_NAME = "help";
	public static final String COMMAND_HELP_SHORTCUT = "h";
	public static final String COMMAND_HELP_DETAILS = "[h]elp";
	public static final String COMMAND_HELP_HELP = "print this help message";
  ```
  
  Each concrete command class has (at least) the following four methods (note that they may be inherited):

  - `protected boolean matchCommand(String)`: checks if the first word of the text introduced by the user
     via the keyboard (minimally processed) corresponds to the command name. It is called by the parse method.

  - `public Command parse(String[])`: checks if the text introduced by the user via the keyboard
     (minimally processed and placed in an array of strings) corresponds to a use of the command, which
     involves checking the number and *syntactic validity* of the parameter values [^4]:

     * If so, it returns an instance of the owning class, which then represents this use of the command.
       In the case of a command with no parameters, the `parse` method can simply return the value `this`.
       But in the case of a command with parameters, to avoid creating *fragile code* [^5], it must return
       a new instance of the command class, rather than changing the values of the attributes
       of the containing object and then returning `this`.

     * If not, it returns the value `null`.
  
  - `public void execute(Game, GameView)`: executes the functionality associated to the command by calling
    a method of the `Game` class, updating the view where necessary (in some commands, this method may also
    perform some other actions). If a command execution fails,
    updating the view consists of printing an error message instead of printing the current state of the
    game.
    
  - `public String helpText()` which returns that part of the text concerning this command that is printed
    on the console by the `help` command. You should be able to place the body of the `helpText` method in
    the `AbstractCommand` class with no need to overwrite it.

- `Controller`: the code of the controller class is now reduced to only a few lines since most of the
  functionality that it included in Assignment 1 is now delegated to the command classes. Note
  that the *Controller* MVC component contains the `Controller` class and the classes of the `Command`
  class hierarchy [^6].


[^4]: **Design principles**: In accordance with the design principle known as *Separation of Concerns*,
syntactic validity of the arguments (whether it is possible to create the internal representation) should
be checked in the parsing of the *Controller* MVC component whereas semantic validity of the arguments
(whether the information of the arguments is in accordance with the logic, in our case, with the rules of
the game) should be checked in the *Model* MVC component. However, the division between syntactic and
semantic validity is sometimes debatable. For example, if the position of a game object is an attribute of
that game object, creating a valid game object by parsing a string such as the argument to the
`addPlant` command inevitably involves checking whether the position is a valid position. If we consider
the position validity to be semantic, we should parse the string and create the corresponding game object
in the *Model* (in our case, in a method of the `Game` class) while if we consider the position validity
to be syntactic, we can parse the string and create the corresponding game object in the *Controller*
(in our case in the `parse` method of the corresponding command class). For reasons that will become clear
in assignment 3, we recommend the former.

[^5]: **Design principles**: Fragile code is code which makes implicit assumptions about its
environment (where this refers to the rest of the program code together with the program environment)
which can lead to it working incorrectly if its environment changes and no longer fulfills these assumptions.
In the case in point, returning the value `this` is making the implicit assumption
that there will only ever be one instance of the corresponding command class in the program at the same time
(the one that is in the `AVAILABLE_COMMANDS` list). Command objects that represent commands without parameters
are *stateless* since, in the absence of attributes, an instance is simply a pointer to a set of methods;
for such objects the above-mentioned assumption is in no way restrictive. However, command objects that
represent commands with parameters are *stateful* and
though the above assumption holds for such commands in the current program, it may not hold for them in evolutions
of the program, e.g. a program where we store a stack of command objects in order to implement an *undo* command,
so why make this assumption, given that it is completely unnecessary to do so?

[^6]: **Design principles**: Already in Assignment 1, ensuring that the *Controller* MVC component did not
contain any logic was in accordance with the design principle known as *thin controllers, fat models*, a
property that we conserve in Assignment 2.
Ensuring that the central part of the *Controller* MVC component, i.e. the `Controller` class, is a high-level
class that does not depend on any concrete implementation of the `Command` interface is in accordance with
the **D** of [the **SOLID** design principles](https://simple.wikipedia.org/wiki/SOLID_(object-oriented_design))
(the ***D**ependency Inversion Principle*).

**Main loop of the program**. In the previous assignment, in order to know which command to execute,
the main loop of the program
in the `run` method of the controller contained a switch or if-else ladder with one option for each of
the commands. In the reduced version of the controller, the `run` method has the
following aspect (your code does not have be be exactly the same but should be similar):

```java
while (!game.isFinished()) {

    Command command = CommandGenerator.parse(view.getPrompt());

    if (command != null) 
        command.execute(game, view);
    else 
        view.showError(Messages.UNKNOWN_COMMAND);
}   
```

Basically, while the game is not finished (due to internal game reasons or to a user exit), the
program reads the text entered by the user, parses it to obtain an object implementing the `Command`
interface and then calls the `execute` method of this object to effect the functionality of the command
entered by the user and to update the view after doing so, if necessary. In the case where the
input text does not correspond to any of the existing commands, the error message
`Messages.UNKNOWN_COMMAND` is printed.

**Genericity of the `Controller` class**. The most important part of the code for the
main loop shown above is the following line assigning a value to the polymorphic variable
`command`:

```java
Command command = CommandGenerator.parse(...);
```
The key point is that the controller is generic: it simply knows how to handle objects that
have an `execute` method (it relies on the `CommandGenerator` invoking a `parse` method and,
for the `help` command, a `helpText` method on those same objects) 
but does not know which concrete command is being executed nor anything about the result of
that execution; the knowledge of what functionality corresponds to each
command is contained in each concrete command class. It is this mechanism that facilitates the
addition of new concrete commands with minimal changes to the existing code. 

**The `CommandGenerator` class**. The `parse` method of this class is a static method
which returns an instance of the concrete command class that corresponds to the text entered
by the user. To this end, the `CommandGenerator` class has a static attribute containing a list
of instances implementing the `Command` interface, concretely, it contains exactly one instance of 
each of the existing concrete command classes. The `parse` method of the `CommandGenerator` method
traverses this list calling the `parse` method of each object on it.
If any of these `parse` methods returns a non-null value (which will be one of the concrete
command classes), the `parse` of the `CommandGenerator` returns this non-null value,
otherwise it returns the value `null`.

The `CommandGenerator` contains another static method `commandHelp` that generates
the output of the help command (so must be called by the `execute` method of the `HelpCommand`
class). Like the `parse` command, it accomplishes its task by traversing the `AVAILABLE_COMMANDS`
list but in this case invoking the `helpText()` method of each object on the list.

The following is a skeleton of this code [^7]:

```java
public class CommandGenerator {

    private static final List<Command> AVAILABLE_COMMANDS = List.of(
        new UpdateCommand(),
        new ResetCommand(),
        new HelpCommand(),
        new ExitCommand(),
        // ...
    );

    public static Command parse(String[] commandWords){
	//...
    }

    public static String commandHelp(){
	//...
    }

}
```

[^7]: For a comparison between `List.of` and `Arrays.asList` see [Difference Between Arrays.asList() and List.of()](https://www.baeldung.com/java-arrays-aslist-vs-list-of)



<!-- TOC --><a name="projections-of-the-game-class"></a>
### Using interfaces to define different projections of the `Game` class

The `Game` class offers services to different parts of the program via its public methods, namely:

- classes of the *Controller* component invoke methods that implement the commands entered by the user; after the above-described refactoring, the calls to these methods are made from the body of the `execute` methods of the `Command` classes. It also invokes methods that return information about the state of the game, such as `isFinished`.

- classes of the *View* component invoke methods that return information about the state of the game needed to display the current state.

- classes of the *Model* component, here the game objects (part of the *Model*, as is the `Game` class itself), invoke methods that concern interactions between game objects. Since these calls *to* `game` from the game objects result from calls *by* `game` to the game objects (via the container, usually as part of an update), they are referred to as *callbacks*.

Notice that, currently, all of the public methods of the `Game` are visible and available to all parts of the application: nothing prevents the *Model* invoking a method of `Game` that was designed for the *Controller* to invoke, e.g. a game object invoking the `reset` method, or the *View* invoking a method of `Game` designed for the *Model* to invoke, e.g. the game printer invoking the `isEmpty` method, etc.  One way of limiting this *coupling* while at the same time specifying more clearly the dependencies between the different components of the application (without
breaking the `Game` class into smaller classes) is to use the Java `interface` construct. The
idea is to use interfaces to define different projections of the public `Game` functionality (i.e. partial views on the services offered by the `Game` class), each containing the methods used by one of the three components of the application, so that
the code of each component can refer only to the corresponding interface, never to the `Game` class itself. The compiler will then detect any attempt to invoke a method that is
not declared on the corresponding interface[^8]. To that end, we define the following three interfaces :

- `GameWorld` to represent the *Model*'s internal view of the services offered by the `Game` class.

```java
public interface GameWorld {

  public static final int NUM_ROWS = 4;
  public static final int NUM_COLS = 8;

  boolean isEmpty(Position position);
  boolean isInsideBoard(Position position);

  // ...
}
```

- `GameStatus` to represent the *View*'s view of the services offered by the `Game` class:

```java
public interface GameStatus {
  int getCycles();
  // ...
}
```

- `GameModel` to represent the *Controller*'s view of the services offered by the `Game` class:

```java
public interface GameModel {

  public boolean isFinished();
  public void update();
  public void reset();
  // ...
}
```

The `Game` class must then implement these interfaces:

```java
public class Game implements GameStatus, GameWorld, GameModel {

  public static final int INITIAL_COINS = 50;

  private GameObjectContainer container;

  // Methods declared in GameModel
  // ...
	
  // Methods declared in GameWorld
  // ...
	
  // Methods declared in GameStatus
  // ...
	
  // Other methods
  // ...
}
```

Finally, in each of the three components of the program, we must replace each occurrence of the type `Game` by the corresponding interface type. For example, the execute method of the `Command` class now has the following form:

```java
public abstract void execute(GameModel game, GameView view);
```

[^8]: **Design principles**: This is in accordance with the **I** of the **SOLID** design principles (the *Interface Segregation Principle*).



<!-- TOC --><a name="game-object-inheritance-hierarchy"></a>
### The game-object inheritance hierarchy

We have seen that the use of inheritance in the `Command` inheritance hierarchy significantly reduces the
repetition of code. Moreover, the use of
inheritance and polymorphism in the *command pattern* greatly facilitates the introduction of new commands,
the key aspect being that the `Controller` class is generic, i.e. it does not handle specific commands
but only handles objects of the abstract class `Command` using polymorphic variables.

Similarly, use of inheritance in an inheritance hierarchy of game objects would also reduce the repetition
of code and facilitate the introduction of new game objects. The key aspect to obtaining the latter benefit
is that the `Game` class and the `GameObjectContainer` class defined below
be generic, i.e. they should not handle specific game objects only objects of an abstract class
called `GameObject`, from which all the concrete game object classes
(currently `Peashooter`, `Sunflower` and `CommonZombie`) derive. So *the code of the `game` and of the
`container` must **not** seek to
identify the dynamic type (i.e. which concrete subclass of `GameObject`) of the objects it is
handling* [^9].

The `GameObject` class should contain all the attributes and methods that are common to all the concrete
game object classes. Each concrete class inherits attributes and methods from the `GameObject` class and
implements its own behaviour by

 - providing implementations for the abstract methods that it inherits,
 - possibly overwriting some of the non-abstract methods that it inherits,
 - possibly adding new methods.

To specify the attributes and methods of the `GameObject` class, we need to think about the behaviour that
is common to all game objects. All game objects have a position on the board so the `GameObject` will contain the
corresponding attribute[^10]. All of them also need an attribute to store the `game` and a
boolean attribute to indicate whether they are alive or not. They have, at least, methods to manipulate their
position and a method to communicate whether they are alive or not. They will also have the method:

```
public void update()
```

Other possible methods: decomposing part of the behaviour of the elements of the game into life-cycle methods
has proved useful:

- `onEnter()`: invoked when this element enters the game
- `onExit()`: invoked when this element leaves or is removed from the game

Note that in many simple objects these methods will be trivial or even empty.

We now provide the skeleton of the code for the `GameObject` class. The `GameItem` interface implemented
by this class is described below.

```java
public abstract class GameObject implements GameItem {

  protected GameWorld game;
  protected Position position;

  GameObject(GameWorld game, Position p) {
    this.game = game;
    this.position = p;
  }

  public boolean isInPosition(Position other) {
    return this.position.equals(other);
  }

  public Position getPosition() { ... }

  abstract public String getIcon();

  abstract public String getDescription();

  abstract public boolean isAlive();

  abstract public void onEnter();

  abstract public void onExit();

  // ...
}
```

Observations regarding the game object hierarchy:

- in the `GameObject` class, the choice of whether to define an abstract method to be overwritten in all
  of the subclasses or a method containing default behaviour (which may
  be simply an empty body) to be overwritten only in those subclasses that do not implement this default
  behaviour, is a design decision that should be made according to which
  option produces the simplest code. For example, in the interest of simplifying the code, you may decide
  not to implement the `GameObject` methods `onEnter` and `onExit` as abstract.
  
- attributes and non-abstract methods should always be placed in the highest class possible of the
  inheritance hierarchy [^12]
  
- anticipating the introduction of more types of zombies and more types of plants, we suggest you also create
  two abstract subclasses of `GameObject` called `Plant` and `Zombie`, where `Peashooter` and `Sunflower`
  derive from the former and `CommonZombie` derives from the latter.



[^9]: **Design principles**: ensuring that `Game` and `GameObjectContainer` are high-level classes that do
not depend on any concrete implementation, is in accordance with the **D** of the **SOLID** design principles
(the ***D**ependency Inversion Principle*). A possible improvement would be to place those of the `GameObject`
methods used by the `Game` and by the `GameObjectContainer` in interfaces and then ensure that the latter
classes only refer to these interfaces, never to the abstract class itself.

[^10]: Consider replacing the `Position` class of assignment 1 (which should have been made immutable)
with a [Java `record`](https://docs.oracle.com/en/java/javase/17/language/records.html).

[^11]: **Design principles**: This is in accordance with the DRY (*Don't Repeat Yourself*) design principle. 

[^12]: `ArrayList` is a class defined in the Java collections library. The classes of this library
make use of *parametric polymorphism*, known as *Java generics* in Java, first
used in functional programming in the mid-70s and introduced in Java in 2004 (with Java 5).
The polymorphism associated with inheritance is called *inclusion polymorphism* or *subtype polymorphism*.
Java generics will be studied in detail in the TP2 course.


<!-- TOC --><a name="game-object-container"></a>
### Delegating functionality of the `game` to the `container`

In the same way as we simplified the `Controller` class, it would also be a good idea to simplify the `Game` class
by reducing its role to that of coordinating the other classes. This can be done by *delegating* much of the functionality
of the `Game` class to other classes, reducing the body of many of its methods to mere invocations of methods of
other classes, which is where the work is really carried out. The main class to which methods are delegated is the
`GameObjectContainer` (this class could be considered to represent the board of the game).

The `GameObjectContainer` encapsulates the data structure used to store the game objects (hereinafter we
will refer to the unique instance of this class as the `container`) as well as the methods for managing them
(updating them, deleting them etc.). For the store itself, we can use any type of collection; for simplicity,
we propose to use the `ArrayList` class[^11], typed using the `List` interface, declared as follows:

```java
public class GameObjectContainer {

	private List<GameObject> gameObjects;

	public GameObjectContainer() {
		gameObjects = new ArrayList<>();
	}
    //...
```

Notice that, thanks to the use of the `GameObject` abstract class and associated inheritance hierarchy:

- The container encapsulates a single data structure for storing all
  the different elements of the game (rather than a data structure for each type of element as in assignment 1).
  It is of great importance that **all** the implementation details of the `GameObjectContainer` be private so,
  for example, it should not export the value of any of the attributes of the `ArrayList` class that it is
  using to store the game objects (such as the number of elements currently in the array). This information
  hiding enables the implementation of the container to be changed without affecting the rest of the program
  code [^13].

- Similarly to the methods of the `Game` class, the methods of the container class do not know which
  game objects are being stored and manipulated, dealing only in abstractions. Neither the
  `Game` nor the `GameObjectContainer` should contain any references to concrete subclasses of the
  `GameObject` class.

- Most of the logic of the game is in the game object methods (i.e. the methods of the concrete
  subclasses of the `GameObject` class). Each such concrete subclass knows how it is updated, what
  happens when it is attacked, etc. We still assume the use of a `ZombieManager` but with a reduced role
  w.r.t. assignment 1, since the zombies themselves are now stored by the container in the common store.

[^13]: **Design principles**: This is in accordance with the design principle known as *Separation of Concerns* or,
more specific to classes in OOP, the *Single Responsibility Principle*, the **S** of the **SOLID** design
principles. As a *sanity check*, imagine, for example, that you decided to change the implementation of the
`GameObjectContainer` to store the game objects in a 2-dimensional array of objects that do not know
their own position, instead of using a list of objects that do know their own position. Would you need to
introduce any modifications in the code of the `Game` class of your implementation? The answer should be no.

<!-- TOC --><a name="game-object-factories"></a>
### Game object factories

**The game-object external-representation format**

The specification of this textual format is as follows:
```
    position concrete-gameobject-name values-of-externally-represented-attibutes-of-that-game-object-instance
```
where none of the currently-used game objects have externally-represented attributes. We clarify with the following examples of valid strings:

- The string:
  ```
  "1-2 Sunflower"
  ```
  represents an object of the class `Sunflower` at position col 1, row 2.

- The string:
  ```
  "0-1 Peashooter",
  ```
  represents an object of the class `Peashooter` at position col 0, row 1. 

- The string:
  ```
  "3-6 CommonZombie",
  ```
  represents an object of the class `CommonZombie` at position col 3, row 6
  
In addition,

- the external representation is not case-sensitive, i.e. the letters it contains may be written in lower-case, upper-case or a mixture of the two,
- the abbreviations ``S``, ``P`` and ``CZ`` can be used for the game objects ``sunflower``, ``peashooter`` and ``commonZombie`` respectively)

**Parsing the external representation in object factories**

Next we add object factories to our implementation, the idea being to separate the object-creation logic from the creation itself.
The mechanism is pratically the same as that of the `Command` pattern. The classes implementing the factories, `PlantFactory` and
`ZombieFactory`, should be placed in the ``gameobjects`` package; both are very similar to the `CommandGenerator`
class. The former has the following method:
```java
	public static Plant parse (String objWords[], GameWorld game);
```
and the latter has the following method:
```java
	public static Zombie parse (String objWords[], GameWorld game);
```
where the array of strings `objWords` contains an external representation (split into tokens). An invocation of this method that creates
the plant or zombie whose external representation (split into tokens) is contained in `objWords` is as follows:
```java
Plant plant = PlantFactory.parse(String objWords[], GameWorld game);
Zombie zombie = ZombieFactory.parse(String objWords[], GameWorld game);

```
Note that the code of the factories is generic (i.e. does not know the dynamic type of the objects being created), except for the list of
available plants in the plant factory:
```java
private static final List<GameObject> availablePlants = List.of(
	new Sunflower(),
	new Peashooter()
	// more plant types will be added later
);
```
and the list of available zombies in the zombie factory:
```java
private static final List<GameObject> availableZombies = List.of(
	new CommonZombie()
	// more zombie types will be added later
);
```
both of which are similar to the list of available commands in the `CommandGenerator` class. The elements of these lists
use no-argument constructors, therefore each concrete subclass of `GameObject` must have such a constructor. What should be
the access modifier for these constructors?

Similarly to the `parse` method of the `CommandGenerator`, the `parse` method of the `PlantFactory` calls a `parse` method in
each plant of the `availablePlants` list, which checks if the `objWords` argument contains an external representation
(split into tokens) of an object of its class and, if so, returns a new instance of that class with the required characteristics,
otherwise, it returns `null`. Similarly with the `parse` method of the `ZombieFactory` class, which calls a `parse` method
in each zombie of the list of `availableZombies`. Observe that the `parse` method of each game object class should parse the
first token of the `objWords` argument (`"<col>-<row>"`) by calling a `parse` method defined in the `Position` class.
Notice that the simplest of the concrete `Plant` and `Zombie` subclasses can simply use a `parse` method
inherited from the `Plant` or `Zombie` class, or even from the `GameObject` class. If convenient, it is also possible for a
`parse` method of a concrete class to complete a superclass `parse` method (see "partial redefinition" in the slides).
To enable the inheritance of `parse` methods that are not allowed to return `this`, the actual creation should be done in a
separate `createInstance` method that is called from the `parse` method and that is overwritten in each concrete `GameObject`
subclass (by a method with a covariant return type).

The plant factory should also have a method similar to the `commandHelp` method of the CommandGenerator, called `listPlants`
which returns the string to be returned by the `list` command. The implementation of this `listPlants` method should be
similar to that of the `commandHelp` command of the `CommandGenerator` class, i.e. via calls to a `getDescription` method
of the game object hierarchy that is similar to the `helpText` method of the command hierarchy. 

**Four commands that use the object factories**

Now we have defined the two factories, we modify the `addPlant` command so that

- its argument is now exactly an external representation (do not forget to change the string contained in the constant attribute
  `COMMAND_ADD_PLANT_DETAILS` of the `Messages` class to `"[a]ddPlant <col>-<row> <plant>"`); if the argument is
  invalid, i.e. it is not the external representation of a plant, including the case where the position is invalid due
  to being off the board, the following error message must be displayed: ``Invalid plant creation: <col>-<row> <plant>``.
- the `execute` method of the `AddPlantCommand` class is implemented via a call to the `parse` method of the `PlantFactory`.

You must also modify the zombie creation mechanism so as to use the `parse` method of the `ZombieFactory`.

Finally, you must define the following debugging commands (i.e. commands that would not be available to the user in a production
version of the game):

- `addZombie` (shorcut: `"z"`, details: `"add[Z]ombie <col>-<row> <zombie>"`, help: `"add a zombie in position (col, row); debug command"`),
   which is almost identical to the `addPlant` command but using the `parse` method of the`ZombieFactory` instead of the
   `parse` method of the `PlantFactory`; it should decrease the number of zombies to appear. If the argument is invalid,
   the following error message should be displayed: ``Invalid zombie creation: <col>-<row> <zombie>``
- `listZombies` (abbreviation `lz`, details: `"[l]ist[Z]ombies"`, help: `"print the list of available zombies; debug command"`),
   which is almost identical to the `list` command but using a `listZombies` method of the `ZombieFactory` instead of the
   `listPlants` method of the `PlantFactory`.

<!-- TOC --><a name="game-item-interface"></a>
### The `GameItem` interface and callbacks

We now turn our attention to the interaction between different elements of the game.
How can we detect when interactions should take place and then execute them?
Solutions involving code in the Game that first uses `instanceof` or `getClass` to discover the
dynamic type of the objects being handled and subsequently implements the interaction behaviour,
along the lines of:
```java
public void update() {
    //...
    GameObject other = game.getGameObjectInPosition(col, row);
    // alternative: other.getClass().getName().equals("Sunflower")
    if(other != null && other.getClass() == Sunflower.class) {  
        ((Sunflower) other).setAlive(false);
    }
    //...
}
```
break encapsulation, break the genericity of the `Game` class, give responsibilities to the `Game`
class which it should not have[^14], use ugly casting, and are **absolutely forbidden**. As are
solutions that use a "DIY" `getClass` / `instanceof`, along the lines of:
```java
public void update() {
    //...
    GameObject other = game.getGameObjectInPosition(col, row);
    if(other != null && other.isSunflower()) {  
        ((Sunflower) other).setAlive(false);
    }
    //...
}
```

We require a solution in which

- each concrete game object class is responsible for its own interaction behaviour and for
  processing its own data, so that modifying or extending this behaviour does not require any
  modification to other classes,
- the implementation of an attack does not involve breaking encapsulation.

Regarding the first point, to that end,
we define `GameObject` methods `receiveZombieAttack` and `receivePlantAttack`,
to implement the behaviour of game objects when they are attacked by a zombie, resp. a plant
(in zombie classes, resp. plant classes, the body of `receiveZombieAttack`, resp.
`receivePlantAttack`, will be empty since zombies do not attack zombies and plants do not
attack plants).

Regarding the second point, in the general case, an attack, or more generally speaking an
interaction, may involve the invocation of several methods on the object that receives the
interaction (e.g. methods querying its state). It would be handy for the attacking object
to be able to ask the `game` which asks the `container` for the game
object on a given position (if any), in order to subsequently invoke as many of its methods as
is required for the interaction. However a method in the `container` and in the `game` that
returns a game object that is on the board would be breaking encapsulation.

In the first assignment, to avoid this encapsulation-breaking, you should have implemented
attacks by ensuring that all calls from the attacking object to the attacked object go via
the `game` (i.e. delegation; in assignment 2 such a chain of delegation would have to include
the `container`). However, using Java
interfaces, the `container` and the `game` *can* return the game object on a given
position *as long as the static type of the game object returned is an interface which severely
restricts what the receiving game object can do with it*. We choose to implement interactions
in this way via an interface called `GameItem`[^15] which currently has only two methods:
```java
public interface GameItem {
    boolean receiveZombieAttack(int damage);
    boolean receivePlantAttack(int damage);
}

public class GameObject implements GameItem {
    // ...
}

public class Zombie extends GameObject {
    //...

    public void update() {
        //...
        GameItem item = game.getGameItemInPosition(col, row);
        if(item != null ) {  
            item.receiveZombieAttack(this.damage);
        }
        //...
    }
}
```
With the above code, the caller of the `getGameItemInPosition` method cannot do
anything with the game object returned by this method except call one of the two attack
methods, since these are the only game object methods that are defined in the static type
(i.e. the `GameItem` interface).

As we extend our program we may modify the `GameItem` interface in order
to add more specific and/or more complex interactions.

[^14]: **Design principles**: Contradicting the **S** of the **SOLID** design principles
(the **S**ingle responsibility principle).

[^15]: **Design principles**: Note that this solution is also more in accordance with the **D**
of the **SOLID** design principles.

<!-- TOC --><a name="generalising-interactions"></a>
### Generalising the interactions between game objects

*The modifications described in this section are not mandatory, in the sense that it is possible
to obtain the top grade without implementing them, but will be assessed, in the sense that their
implementation may contribute to improving a grade which is not the top grade.*

In the general case, interactions may affect both of the objects involved in the interaction. For
example, we could imagine introducing a zombie with a shield which bounces peas back to the peashooter
that fired them, damaging it in the process. How could we accomplish this? Since interactions must be
implemented using method calls, it is inevitable that they are not implemented in a symmetric manner,
since the method implementing the interaction must belong to one of the objects involved. In an
interaction involving two parties, one object (the caller of the method)
initiates the interaction and the other (the callee of the method) receives it. In a game where
one game object is attacking another, which one initiates the interaction is clear but this may
not always be the case. Ideally, the run-time selection of the code to be executed for an
interaction (this selection being known as *dispatch*) would depend on the dynamic
type of *both* of the objects involved in that interaction. This is known as ***double-dispatch***.
Taking into account the asymmetry in the implementation of interactions using method calls,
could we implement double dispatch in Java in the following manner?:

- firstly by having the caller of the `receiveInteraction` method pass itself as an argument
  (with a generic static type such as `GameItem`) to the interaction method call
- secondly, by having the dispatch of the `receiveInteraction` method depend not only on the dynamic
  type of the method owner, the callee, i.e. the usual dynamic-binding case, but also on the dynamic type
  of the method argument, the caller  (or, more generally, on that of one of the method arguments, for
  double-dispatch, or on that of several of the method arguments, for multi-dispatch).

The answer to this question is "no", since the second step would require that method arguments be
covariant (see the slides about covariance and contravariance), which is not the case in Java.
Moreover, for type-safety reasons, covariant arguments
are not considered desirable. However, we can implement a *"semi" double dispatch* in Java, in which the
interaction depends on the dynamic type of one of the participants and the static type of the other.
In our asymmetric implementation,

- firstly, the interaction method caller passes itself as an argument (with a static type that coincides
with its dynamic type) to the interaction method call; this argument will be used to execute a
*callback*
- secondly, the implementation of the interaction depends on the dynamic type of the method owner
(i.e. uses overwriting / dynamic binding which is resolved at execution time) and the static type of the
method argument (i.e. uses overloading which is resolved at compile time).
```java
  public class Peashooter extends Plant {
    //...

    public void update() {
      //...
      GameItem item = game.getNextZombieOnRow(col, row);
      if(item != null )
        item.receiveInteraction(this);
      //...
    }
  }
  
  public class ShieldedZombie extends Zombie {
    //...

    public receiveInteraction(Peashooter peashooter) {
      //...
      peashooter.receiveInteraction(this)
      //...
    }
  }

```
Note that the `this` argument of the call to `receiveInteraction` in Peashooter and of the call
to `receiveInteraction` in ShieldedZombie will be resolved at compile time, using the static type
of the owning object, so the method in which it is called (here `update` of Peashooter and
`receiveInteraction` of `ShieldedZombie`) cannot be inherited from a superclass.

The disadvantages of using this "semi" double-dispatch are as follows:
- the use of overloading instead of overwriting may involve duplication of code
- passing an object as an argument (here, the object referred to by `this`) typed using its
  static type instead of using a restricted type such as `GameItem` is, strictly speaking,
  breaking encapsulation.
However, we consider that the advantages outweigh the disadvantages and that this is a useful
mechanism to learn.

The effect of this "semi" double dispatch on the game objects is to replace a set of methods
of the type `receiveInteractionFromConcreteGameObject` with a set of methods of the type
`receiveInteraction(concreteGameObject)`. We must therefore change the `GameItem` interface
accordingly (we also take the chance to extend it with some query methods):

```java
public interface GameItem {

  public boolean isAlive();
  public boolean isInPosition(Position pos);

  // we could use subclasses of Plant and Zombie for more specific interactions
  public boolean receiveInteraction(Zombie zombie);
  public boolean receiveInteraction(Plant plant);
}
```


<!-- TOC --><a name="pruebas"></a>
## Testing

*** To be added shortly ***
