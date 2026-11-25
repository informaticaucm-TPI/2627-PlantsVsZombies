<!-- TOC start -->
- [Assignment 3: Exception handling and file handling](#practica-3-excepcionesYficheros)
- [Introduction](#introduccion)
- [Exception handling](#exceptions)
	- [Exceptions thrown in the *Control* part of the program](#command-exceptions)
	- [Exceptions thrown in the *Model* part of the program](#gamemodel-exceptions)
- [File handling](#files)
 	- [Serialization / deserialization](#serialization)
	- [Saving the game state to file: the `save` command](#save-command)
	- [Loading the game state from file: the `load` command](#load-command)
	- [Adapting the `reset` method of the `Game` class](#reset-load-game)
<!-- TOC end -->

<!-- TOC --><a name="practica-3-excepcionesYficheros"></a>
# Assignment 3: Exception-handling and file handling

**Submission: December 2nd 2026, 12:00**

**Objectives:** handling exceptions and files


<!-- TOC --><a name="introduccion"></a>
# Introduction

In this assignment we extend the functionality of the Plants vs Zombies game of the previous
assignment in two ways:

- *Exception handling*: errors that may occur during the execution of the application
can be more effectively dealt with using the exception mechanism of the Java language.
An exception object encapsulating any information about an error and its context that
is considered to be of interest, in particular, an error message, is created at the
point in the code where the error occurs and is then passed between the methods of
the call stack. As well as making the program more robust, this mechanism enables the
user to be informed about the occurrence of an error in whatever level of detail is
considered appropriate, while at the same time providing a great deal of flexibility
in regard to where the error is handled (in particular, to print an error message).

- *File handling*: a useful addition to the application is the facility to save the
state of a game to file and load the state of a game from file. To this end,
we add two new commands, one to write to a file and the other to read from a file.
The use of the *Command Pattern* introduced in the previous assignment greatly
facilitates the addition of the new commands `save` and `load`.
The use of the factories introduced in
the previous assignment greatly facilitates the implementation of the `load` command.
  
<!-- TOC --><a name="exceptions"></a>
# Exception handling

In this section, we present the exceptions that should be handled by the application and
give some information about their implementation. The section is divided into two parts,
the first dealing with exceptions that may be thrown in the control part of the
application, in particular, in the parsing of the user input, the second dealing with
exceptions that may be thrown in the model part of the application, in particular, in
the implementation of the execution of the commands.

It should be pointed out that file handling inevitably involves exception handling,
particularly when reading data from files, which is why these two topics are often
introduced to students at the same time. Exceptions that may be thrown during file handling
(i.e. during the execution of the `save` and `load` commands, or during the loading of the
initial state) will be dealt with in the file-handling part of this document.

You will have observed that there are circumstances in which a
command may fail, either in its parsing or in its execution. For example, the parsing
of the `add` command will fail if the user provides a parameter that cannot be
parsed as a valid plant. In the previous assignment, on occurrence of such errors,
the `parse` method of the the `CommandGenerator` class simply
returned `null`. However, this does not transmit any indication about
the reason for the occurrence of the error, nor does it permit the transmission of any
other data about the error that may be required to handle it correctly.
As another example, the execution of the `add` command will also fail if the
position of the object given by the user is outside the board or already contains an object.
In the previous assignment, the occurrence of such an error was communicated to
the `execute` method of the `AddPlantCommand` class via a boolean return value
(or by calling methods in the `game` to check such conditions which could be
considered as moving code that should be in the *Model* into the *Controller*).
Returning the value `false` to communicate the occurrence of an error has similar
problems to returning `null` for this same purpose.

In this assignment, we deal with these issues using the Java exception mechanism. An
exception mechanism provides a flexible communication channel between the location in
the code where an error occurs and the location in the code where that error is handled,
along which any required data concerning the occurrence of the error can be sent from
the former to the latter[^1]. In many cases, the data concerning the occurrence of the error
that is transmitted from one code location to another via an exception consists simply of
an error message (as well as the stack trace which the system includes in any exception),
and the handling of this error consists simply of sending that message
to the standard error to be displayed on the screen (along with the stack trace).
In the general case, however, more
data about the error and its context may be transmitted between code locations and the
error-handling may require more complex actions than simply printing a message to the
screen. In this application, the exception mechanism enables us to ensure that
the controller is responsible for sending all error messages to the view to be displayed.

[^1]: The error code mechanism of C is somewhat primitive in comparison, though it is also much
less computationally costly, which is why C++ retains it as well as having an exception mechanism
(though this exception mechanism is less type-checked and more difficult to use than the Java
equivalent)

For simplicity, we recommend that you place all programmer-defined exception classes
in a package called `pvz.exceptions`.


<!-- TOC --><a name="command-exceptions"></a>
## Exceptions thrown in the *Control* part of the program

We define a new exception class called `CommandException` and two subclasses:

- `CommandParseException`: used for exceptions that occur during the parsing of a command
  (so during the execution of the `parse` method of the `Command` class)
	
- `CommandExecuteException`: used for exceptions that occur during the execution of a command
  (so during the execution of the `execute` method of the `Command` class)

### Parsing errors

First we deal with the occurrence of errors during the parsing of the commands, which will lead
to the throwing of a `CommandParseException`, instead of returning `null` as was done in the previous
assignment. 

The `parse` method of the `Command` class must now be declared to throw exceptions of type 
`CommandParseException`.

```java
  public abstract Command parse(String[] parameter) throws CommandParseException;
```
For example, the `parse()` method of the `NoParamsCommand` can now be implemented as follows:

```java
public Command parse(String[] commandWords) throws CommandParseException {
    // in fact, commandWords.length == 0 not possible due to strip() in getPrompt() method
    if commandWords.length != 0 && matchCommandName(commandWords[0]))
        if (commandWords.length > 1)      // there are extraneous parameters
            throw new CommandParseException(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER);
        else
            return this
    return null
  }
```

Note that the `parse` method of the commands must only throw an exception if the first word
of the input data matches the command name and there is an error in the command
arguments. Not matching the command name is not an error but simply an indication that
the input text does not correspond to this particular command so, in this case, the `parse` method
must not throw an exception and must return `null` as it did in the previous assignment,
to enable the `parse` methods of the other commands to also check for a match.

The `parse` method of the `CommandGenerator` class must now also be declared to throw exceptions of
type  `CommandParseException` in order to transmit to the controller the exceptions thrown by the
commands:

```java
	public static Command parse(String[] commandWords) throws CommandParseException
```

but also to transmit exceptions of type `CommandParseException` thrown by itself, in the case
where none of the `parse` methods of the commands recognises the first word of the
input data, i.e. the case of an unknown command (instead of returning `null` and having the
controller handle this case with an *if-then-else*):

```java
	throw new CommandParseException(Messages.UNKNOWN_COMMAND.formatted(commandWords[0]));
```

The `NumberFormatException` is a system exception that is thrown when an attempt is made to parse
the `String`-representation of a number and convert it to the corresponding value of type
`int` or `long` or `float`, etc., in the case where the input string does not, in fact, represent
a number and cannot, therefore, be so converted. A `NumberFormatException` should be caught
in the `parse` method of the command where it occurs and wrapped in a `CommandParseException`.
The term "wrapping" refers
to the good practice of catching a lower-level exception and throwing a higher-level exception
which includes it (accomplished by passing the lower-level exception as argument to the constructor
of the higher-level exception) and *which contains a less specific message than the low-level one*[^2].

[^2]: You will probably have seen examples of bad practice in this regard, e.g. electronic commerce web
applications that produce messages for the user of the type "SQLException...". The user does not care about
this level of implementation detail and should not be receiving this type of message.

For example, in the `parse` method of the `ResetCommand` class with a level argument you should do
something similar to the following:

```java
	} catch (NumberFormatException nfe) {
		throw new CommandParseException(
			Messages.LEVEL_NOT_A_NUMBER_ERROR.formatted(commandWords[1]), nfe);
	}
```

### Execution errors

An error that occurs during the execution of a command will lead to the throwing of a
`CommandExecuteException` in the body of the `execute` method of the command classes. This exception
will be used to wrap lower-level exceptions thrown on the occurrence of errors in the *Model*
part of the application (we deal with these errors in detail in the next section).

The `execute` method of the `Command` class must now be declared to throw exceptions of type 
`CommandExecuteException`:

```java
	public abstract void execute(GameModel game, GameView view) throws CommandExecuteException;
```

### Capturing both parsing errors and execution errors in the controller

As already stated, both types of exceptions, `CommandParseException`s and `CommandExecuteException`s,
reach the controller, which is responsible for catching them and sending the 
messages they contain to the view for display. In the case where an exception wraps a lower-level
exception (which may, in turn, wrap an even lower level exception etc.), in our application,
the controller sends all levels of error messages to the view for display. 

The `run()` method of the controller will now contain the following code. Notice that to avoid
extra lines of code, we are using the fact that in Java, as in the C language, the assignment
operator evaluates to the value being assigned (in the code `(wrapped = wrapped.getCause()) != null`):

```java
	public void run() {
		...
		while (!game.isFinished()) {
			...
			try { ... }
			catch (CommandException e) {
				view.showError(e.getMessage());
				Throwable wrapped = e;
				// display all levels of error message
				while ( (wrapped = wrapped.getCause()) != null )
					view.showError(wrapped.getMessage());
			}
		}
		...
	}
```

In a real application, of course, a normal user would
almost certainly not be interested in seeing the messages from all the different levels (see the
above footnote about the bad practice in many electronic commerce applications of displaying
*SQLExceptions* to normal users), though an administrator may be, and comprehensive information
about errors is also what is required if it is to be written to an error log.

Finally, notice that, usually, both the parsing and the execution of commands with parameters 
throw more types of exception than do the parsing and execution of commands without parameters.
In the case of the `add` command, for example, the parsing of its parameters may generate
`CommandParseException`s, or system exceptions that we wrap in `CommandParseException`s, and its
execution in the model part of the application may generate exceptions that we wrap in
`CommandExecuteException`s.

<!-- TOC --><a name="gamemodel-exceptions"></a>
## Exceptions thrown in the *Model* part of the program

As stated above, the `execute` method of the `Command` class must now be declared to throw
exceptions of type `CommandExecuteException`:

Most, if not all, of the errors that occur during the execution of commands, arise in the game logic,
that is, in the *Model* part of the program. In the previous assignment, the methods in which the
errors occurred may have returned a `boolean` value to indicate whether the execution had succeeded
or failed. For example, the method of `GameModel` called to add a plant returned `false` when the
position passed as argument was outside the board or when
the parameter did not correspond to any known game object. However, returning the value `false` did
not permit these two types of error to be distinguished[^3]. Generating error data, in particular an
error message, at the point in the code where the error occurs enables such distinction. We could
repurpose the boolean return value and maintain it as an indication of whether or not the game state
has been modified (we could also simply return `void` instead).

[^3]: If, instead of having the method return false, you checked this and other conditions from
the `execute` method of the `AddPlantCommand`, `AddPlantCheatCommand` or `AddZombieCommand`,
you should now remove these checks in order to use the exceptions policy known as EAFP rather than
that known as LBYL.

Note that with some commands, some of the parsing of the command is carried out in the *Model*
part of the application as part of the command execution. For example, the *Model* is responsible
for parsing the arguments to the `add` command since this is carried out by calling the `parse`
method of one of the factories which calls the `parse` method of the game objects. Therefore, the
*Model* part of the application can also throw parsing exceptions during the execution of commands
such as the `add` command, i.e. in calls made from the `execute` method of the `add` command. Like
all exceptions thrown during the exeuction of a command, these exceptions should be caught in the
`execute` method of the corresponding command class and rethrown wrapped in a
`CommandExecuteException`.

The program should now display the following error messages for the two cases discussed
above (notice the two messages, one from each level of exception):

- On trying to add an unknown type of game object:
	```
	[DEBUG] Executing: add 3-2 poTaTo

 	[ERROR] Error: Command execute problem
 	[ERROR] Error: Unknown game object: "3-2 poTaTo"
	```
- On trying to add an object in a position that is outside the board:
	```
	[DEBUG] Executing: add 4-24 Sunflower

	[ERROR] Error: Command execute problem
	[ERROR] Error: Object position is off board: "(4,24) Sunflower"
	```
We handle the first of these erorrs by defining a new exception class `ObjectParseException` to be
thrown by the `parse` of the factories when an attempt is made to parse a string, the object
part of which is not the name of any known game object (c.f. the exception thrown by
the `parse` method of the `CommandGenerator`). We handle the second by
defining a new exception class `OffBoardException` to be thrown when an attempt is made to
use a position that is outside the board. The method(s) of `GameModel` called to add a plant or
a zombie must now be declared to throw these two exceptions (and more, as we see below), e.g.:

```java
public boolean addGameObject(...) throws OffBoardException, ObjectParseException;
```

We also define the following new exception class to indicate unsuccessful parsing of
text that is expected to represent a position:

  - `PositionParseException`: to be thrown by the `parse` method of the `Position` class 
     (called by the `parse` methods of the factory classes) when an attempt is made to
     parse a string that is not the textual representation of a position string, i.e. does not
     respect the syntax for a position string (note that this does not concern whether or not
     the position is off the board). This exception may be thrown during the parsing of the
     `add`, `addZombie` or `cheatPlant` commands as well as during the execution of the `load`
     command, which inevitably also involves parsing, as we see below.

For convenience, we organise the above exceptions in an inheritance hierarchy using two new
Exception class `GameModelException` and `GameModelParseException` as follows:
- `GameModelException` has subclasses:
   * `OffBoardException`
   * `GameModelParseException` which has subclasses:
      + `ObjectParseException`
      + `PositionParseException`

Since the method(s) to add a plant or a zombie throw `OffBoardException`s, `ObjectParseException`s,
and `PositionParseException`s we can now simplify its/their declaration as follows:
```java
public boolean addGameObject(...) throws GameModelException;
```
Similarly, we can simplify the declaration of the `parse` methods of the factory classes
as follows:
```java
public boolean parse(String[] objWords) throws GameModelParseException;
```

Observe that, as is the case for the `CommandGenerator`, the `parse` method of the factories
never returns `null`; either the parsing is successful or an exception is thrown [^4].

[^4]: This is in accordance with the behaviour of library methods such as
[Integer.valueOf](https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html#valueOf-java.lang.String-). 

Exceptions thrown in the *Model*
part of the application during the execution of a command should be caught in the `execute`
method of the command class and wrapped in a `CommandExecuteException`, for example:

```java
} catch (GameModelException gme) {
	throw new CommandExecuteException(Messages.ERROR_COMMAND_EXECUTE, gme);
}
```

With this procedure, no information is lost since the wrapped exception can be recovered
using the `getCause` method of the `Throwable` class (see the above code for the `run`
method of the controller).


<!-- TOC --><a name="files"></a>
# File-handling

<!-- TOC --><a name="serialization"></a>
## Serialization / deserialization

In computing, the term *serialization* refers to converting computing structures into a stream
of bytes usually with the objective of saving this stream to a file or transmitting it on a network.
Commonly, the structure being serialized is the current state of an executing program, or of part
of an executing program. The term *deserialization*
refers to the inverse process of reconstructing the computing structures, commonly the state of
an executing program, or part of an executing program, from a stream of bytes.
Serialization to /deserialization from a text stream is sometimes referred to as
stringification/destringification. Clearly, the format used for serialization/stringification
should be designed in such a way as to facilitate deserialization/destringification.

Java incorporates a generic serialization mechanism capable of converting any Java objects,
and therefore any executing Java program or part of any executing Java program,
into a binary stream (see classes `ObjectInputStream` and `ObjectOutputStream`).
We do not require such a generic serialization/deserialization
mechanism; our interest is simply to serialize to, and deserialize from, a text stream
that represents the current state of our game, with a view
to writing this state to, and reading this state from, a text file [^5]. Note that the textual
representation currently produced by the view is not a suitable format for serialization since
deserialization of this format would be a rather complicated enterprise. We therefore
need to define a text-serialized format, in which the state of the game is represented as
the global values followed by a sequence of game elements, each of which is represented as
a sequence of words in a specified format. For this specified format, we can use the external
representation introduced in the previous assignment to be used with the `add` and
`addZombie` commands (used as the format for the command argument argument). To enhace the
readability of the serialized content, we separate the external representation of each game
object with a line break.

[^5]: To simplify, we do not save the state of the pseudo-random number generator so that
an execution containing a serialization/deserialization will not have the same behaviour
as the same execution without the serialization/deserialization.

### Serialization format

We explain our text-based serialization format via a simple example:
```
HARD 28 10 2 18 0
0-0 Sunflower 1 2
1-0 Peashooter 3
5-0 CommonZombie 5 2
5-2 SportingZombie 2
5-3 ExplodingZombie 5 1
7-1 BucketheadZombie 8 2
2-0 Sun 6
2-0 Sun 3
4-2 Sun 2
```
The information on the first line represents, from left to right:

- the level
- the cycle number
- the number of sun coins
- the remaining zombies
- the number of generated suns
- the number of caught suns

The information on the following lines represents the state of a game object, according to the external
representation format described in part II of the Assignment 2 problem statement with the
addition of attribute values: the remaining lives (or remaining cycles for suns) and, for some game
objects, an internal counter value. From top to bottom:

- in position (0,0), a Sunflower with 1 remaining life and internal-counter-value 2,
- in position (1,0), a peashooter with 3 remaining lives,
- in position (5,0), a common zombie with 5 remaining lives and internal-counter-value 2,
- in position (5,2), a sporting zombie with 2 remaining lives,
- in position (5,3), an exploding zombie with 5 remaining lives and internal-counter-value 1,
- in position (7,1), a buckethead zombie with 8 remaining lives and internal-counter-value 3,
- in position (2,0), a Sun with 6 remaining cycles,
- in position (2,0), a Sun with 3 remaining cycles
- in position (4,2), a Sun with 2 remaining cycles.

The internal counter values may be different in your implementation since you may have stored such a counter
in the form of
"cycles-to-next-move" (for zombies) / "cycles-to-explosion" (for cherrybomb) / "cycles-to-next-sun-generation" (for sunflowers)
or in the form of
"cycles-since-last-move" (for zombies) / "cycles-since-creation" (for cherryBomb) / "cycles-since-last-sun-generation" (for sunflowers)
or in the form of
"game cycle on creation" (action or no action on a given cycle calculated using the appropriate mod operation)
etc. Note that neither a peashooter nor a sportyZombie needs an internal counter since they perform their
action on every cycle.


<!-- TOC --><a name="save-command"></a>
## Saving the game state to file: the `save` command

Having defined our serialization format, in fact a stringification format, the next logical
step is to introduce a new command, the `save` command, which can be used to save the
serialized current state of the game in a file. In accordance with OOP principles, each class
should produce its own "stringification".

Recall that the purpose of the Java `toString()` method is to return a human-readable textual
representation of the owning object. Since our serialization is to be stringification, we
can use the `toString` method of the game objects for this purpose. To this end, we introduce
a new method in the `Game` class [^6] (and the `GameModel` interface):
```java
	public void save(String fileName) throws GameSaveException {...}
```
This method, to be called by the `execute` method of the `SaveCommand` class, can be implemented
by simply calling the `toString` method of the `game`, which calls the `toString` method of the
`container`, which calls the `toString` method of each of the game objects, and then writes
the resulting string to file [^7] (regarding the type of stream, we suggest using a buffered
`FileWriter`, opened using `try-with-resources`). Exceptions occuring during the execution of
the `save` method
should be caught and wrapped in a `GameSaveException`, a new subclass of `GameModelException`
(which must then, in turn, be caught and wrapped in a `CommandExecuteException`) [^8].

The output of the `help` command should now include details about the `save` command:
```
Available commands:
   ...
   [s]ave <fileName>: save the actual configuration in text file <fileName>
   ...
```
Regarding the `parse` method of the `SaveCommand` class, the simplest way to
implement a `save` command would be without parameters, the name of the file to be
used being "hardwired", i.e. stored in a constant attribute of the `SaveCommand`
class, allowing only one game state to be saved at any one time. Alternatively,
the `save` command could be implemented with one `String` parameter: the file name.
As can be seen from the output of the `help` command, in this assignment you must
implement the `save` command with a file-name parameter, so the `parse` method of
`SaveCommand` will have to deal with this parameter. It is typical for the `save`
command to add a file extension, we suggest `.pvz`, so the user does not have
to provide it.

[^6]: Which class should be responsible for opening the input character stream is a
not-so-obvious design decision.
Instead of the `execute` method of the `SaveCommand` class passing the filename as an
argument to the `save` method for it to then open the input character stream as
suggested here, the `execute` method could open the input character stream and then
pass it as an argument to the `save` method, rather than passing the file name.

[^7]: It could be argued that the textual representation being saved to file is a view in
the sense of the MVC architecture and that the *View* part of the application should
therefore be reponsible for generating it. This could easily be accomplished by simply
moving the `save` method from the `game` to the view, so that the view is responsible for
calling the `toString` method of the `game`. However, as a counterargument, should a binary
serialization, rather than a "stringification", also be considered a view?

[^8]: Though not stated in the Java specification, the usual policy is that when a program
attempts to open an output stream to a file that does not exist, it is created (it *is*
stated in the Java specification that if the file already exists, it is overwritten, unless
opened in *append* mode). However, a `FileNotFoundException` may still be thrown on trying
to open an output stream since it is thrown in cases other than the absence of a
file with that name, e.g. if the filename cannot be correctly resolved (see the Java
documentation).

<!-- TOC --><a name="load-command"></a>
## Loading the game state from file: the `load` command

In the following sections we present an implementation of a new command, the `load` command,
used to read a state of the game from a file. The implementation of the `load` command
is inevitably more complicated than that of the corresponding `save` command though we
have already defined the serialization format and, moreover, we have already created most
of the code that parses this format orchestrated by the factory classes. We divide
the task of implementing the `load` command into the following subtasks:

- [modify the working of the factory classes and add exceptions](#game-object-factory),

- [define a `GameConfiguration` interface](#game-configuration) to be implemented by classes
  representing a game configuration, i.e a valid state of the Mario game,

- [create a `FileGameConfiguration` class](#game-configuration-loader), responsible for
  loading the serialized data from file and storing it in a game configuration,
  delegating part of the task to the factories.

- [add the implementation of the load command to the `Game` class](#game-load),

- [create the `LoadCommand` class](#load-command-class).

Note that the behaviour of the `reset` command will need to be adapted to the existence
of the `load` command since reset should mean return to the last loaded state.

<!-- inner TOC --><a name="game-object-factory"></a>
### Modifying the factory mechanism and adding exceptions

We need to modify the `parse` methods of the game object classes in order to handle
optional attribute information. If this information is present in the external representation
following the name of the game object (factory `parse` of data loaded from file), the
corresponding game object is created with the specified attribute values; if not (factory
`parse` of a command argument), it is created with initial values as in the previous
assignment.

We also need to define a `GameObjectFactory` class that is mostly just a wrapper for the
two existing factories: its `parse`method checks whether the text corresponds
to the external representation of a game object, by using the `parse` methods of the
`PlantFactory` the parse method of the `ZombieFactory` and a call to the `parse` of any
game object that is neither a zombie nor a plant (currently, only the `Sun` class).
Recall that the `parse` method of each of the factories now throws
`GameModelParseException`s if the text is not a valid external representation of a
plant, respectively a zombie. Accordingly, the `GameObjectFactory` `parse` method should
proceed as follows:

1. Call the `parse` method of the `PlantFactory`, returning the value that it returns.
2. If it throws a `GameModelParseException` (indicating that the text contains
   a syntactically-invalid position or is not the external representation of a known plant),
   catch this exception and call the `parse` method of the `ZombieFactory`, returning
   the value that it returns.
3. If this method also throws a `GameModelParseException` (indicating that the text
   contains a syntactically-invalid position or is not the external representation of a
   known zombie) catch this exception and call the `parse` method of any non-plant,
   non-zombie game objects (so in the current implementation, only the `Sun` `parse`).
   If any of them returns a non-null value, return
   that value, otherwise throw an `ObjectParseException`. This is assuming that the
   call to the `parse` of the `Position` class is performed in the `parse` of the
   game objects; if, in your code, it is the `parse` method of the factories that
   calls the `parse` of the `Position` class, modify this procedure accordingly.

<!-- inner TOC --><a name="game-configuration"></a>
### Representing the loaded state of the game: the `GameConfiguration` interface

A game state can be encapsulated in an object that stores the different components of such
a state. The corresponding class must provide methods to access the different components
of this state, i.e. it must implement the following interface (to be placed in the 
`pvz.logic` package:

```java
public interface GameConfiguration {
    public Level getLevel();
    public int getCycleNumber();
    public int getSunCoins()
    public int getRemainingZombies();
    public int getGeneratedSuns();
    public int getCaughtSuns();
    
    // not breaking encapsulation since not yet the value of an attribute of the game
    public List<GameObject> getGameObjects();
}
```
If you prefer, the last method could even be replaced with the method:
```java
    public GameObjectContainer getContainer();
```

<!-- inner TOC --><a name="game-configuration-loader"></a>
### Reading the file data: the `FileGameConfiguration` class

As a general principle, reading from a file must never:
1. cause a program to crash,
2. leave it in an incoherent state
   
How do we accomplish this?
1. Catching all exceptions that could be thrown when reading from a file ensures that
  the program cannot crash. If the load fails, the program can handle the exception,
  informing the user, and continue the program exactly as if the load had not been
  attempted. To this end, to be sure that no exception will go uncaught, after catching
  specific exceptions, your code should include a clause catching a general exception,
  `} catch Exception e {`, wrapping it, as for the other exceptions thrown during
  loading, in a `GameLoadException,` see below.
2. Loading the file data into a special-purpose object which is then only used (in our
  case as the new state of the game) if and when the data has been completely and
  successfully loaded from file is one way of ensuring that the program cannot be left
  in an incoherent state. In our case, this special-purpose object is an instance of the
  `FileGameConfiguration` class which implements the `GameConfiguration` interface and
  is to be placed in the `tp1.logic package`. Ensuring that an incoherent game state
  can never be used as a working game state is facilitated by performing the loading
  from file in the constructor of the `FileGameConfiguration` class. Thus, if the
  checking of validity is exhaustive, objects of this class that encapsulate an
  incoherent game state can never exist.

The `FileGameConfiguration` constructor has two parameters:
- a `String` containing the name of the file from which the game state is to be loaded [^9].
- the game, typed as `GameWorld`, to be passed to the constructor of the game objects
  (via the `parse` method of each game object, via the `parse` method of `GameObjectFactory`).

```java
public FileGameConfiguration(String fileName, GameWorld game) throws GameLoadException;
```

[^9]: The design decision discussed in a previous footnote also affects this
constructor. We could have used an input character stream as the parameter instead of
the file name string, in which case, the `game` would be responsible for opening this
input character stream. This alternative is more flexible since it allows the source
of the input character stream to be a file or something else (though it should perhaps
also involve changing the name of the class from `FileGameConfiguration` to, say,
`GameConfigurationImpl` to reflect this generalisation).

The constructor opens the input character stream connected to the file (regarding the
type of stream, we suggest using a buffered `FileReader`, opened using `try-with-resources`),
reads the
data from the first line of the file into attributes of the `FileGameConfiguration`
class, then reads the following lines of the file, passing the text of each to the
`parse` method of the `GameObjectFactory` to obtain a game object to be added to
an attribute storing the list of game objects (or storing a whole new container, if
you prefer). This constructor can throw the following programmer-defined
exception:

- `GameLoadException` (a new subclass of `GameModelException`): used to wrap any exception
  thrown during loading, such as `FileNotFoundException`, any operating system exception,
  `ObjectParseException`, `OffBoardException`, `PositionParseEsception` or any other
  exception thrown due to an incorrect file format [^10]. Such an exception will itself
  be wrapped in a `CommandExecuteException` in the `execute` method of the
  `LoadCommand` class.

[^10]: To be exhaustive about this, the validation should also check many other conditions,
such as that values are within accepted ranges, and raise a suitable exception if not.
However, this would require a great deal of work and is certainly **not** obligatory.

<!-- inner TOC --><a name="game-load"></a>
### Adding the implementation of the load command to the `Game` class

We need to add the possibility of loading the state of the game from file to the model
part of the application. To this end, we add a function called `load` to the `Game` class
(and to the `GameModel` interface): 

```java
	public void load(String fileName) throws GameLoadException {...}
```

This method simply creates a `FileGameConfiguration` object, typed as a `GameConfiguration`
and, if this creation is successful, sets the attributes of the `game` (and possibly the
`container`) to the values returned
by calls to the methods of the `GameConfiguration` interface. It must declare that it throws
the same exceptions as the constructor of the `FileGameConfiguration` class.


<!-- inner TOC --><a name="load-command-class"></a>
### The `LoadCommand` class

We can now implement the `LoadCommand` class to enable the player to ask the game to
load a game state from file. After successfully loading such a game state, the `execute`
method of this class must display it.

The output of the `help` command should now include details about the `load` command:
```
Available commands:
   ...
   [l]oad <fileName>: load the game configuration from text file <fileName>
   ...
```

Regarding the `parse` method of the `LoadCommand` class, as for the `save`
command, you must implement the `load` command with a file-name parameter so
the `parse` method of `LoadCommand` will have to deal with this parameter.
The `load` command may, or may not, require that the user also provide the 
file extension. Here we suggest that the user not be obliged to provide it.

<!-- TOC --><a name="reset-load-game"></a>
## Adapting the `reset` method of the `Game` class

Using the no-argument `reset` command in a game that has previously been loaded from file should
have the effect of placing it in the state it was in immediately after the last use of the `load`
command. This can be accomplished by
having the `load` method of the `Game` class  store the `FileGameConfiguration` object created
during loading in an attribute of the `Game` class, e.g.:
```java
private GameConfiguration previousConfig;
```
The value of the `previousConfig` attribute being `null` serves to indicate that the reset should
return the game to the initial configuration.

**Warning**: be sure to check that the new implementation of the `reset` command works correctly,
since you may have encapsulation/shared-memory problems in the `FileGameConfiguration` class which
prevent it from doing so.

