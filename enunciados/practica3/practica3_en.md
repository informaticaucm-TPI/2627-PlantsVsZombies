<!-- TOC start -->
- [Assignment 3: Exception handling and file handling](#practica-3-excepcionesYficheros)
- [Introduction](#introduccion)
- [Exception handling](#exceptions)
	- [Exceptions thrown in the *Control* part of the program](#command-exceptions)
	- [Exceptions thrown in the *Model* part of the program](#gamemodel-exceptions)
- [File handling](#files)
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

Note that with some commands, some of the parsing of the command is carried out in the *Model*
part of the application. For example, the *Model* is responsible for parsing the arguments to
the `add` command since this is carried out by calling the `parse` method of one of the factories
which calls the `parse` method of the game objects. Therefore, the *Model* part of the application
can also throw parsing exceptions during the execution of commands such as the `add`
command, i.e. in calls made from the `execute` method of the `add` command (and also during the
execution of the `load` command, see below). The latter exceptions should be caught in the
`execute` method of the corresponding command class and rethrown wrapped in a
`CommandExecuteException`.

### Capturing parsing errors and execution errors in the controller

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

Some of the errors that occur during the parsing of commands and most, if not all, of the errors
that occur during the execution of commands, arise in the game logic, that is, in the *Model* part
of the program. In the
previous assignment, the methods in which the errors occurred may have returned a `boolean` value
to indicate whether the execution had succeeded or failed. For example, the method of `GameModel`
called to add a plant returned `false` when the position passed as argument was outside the board or when
the parameter did not correspond to any known game object. However, returning the value `false` did
not permit these two types of error to be distinguished[^3]. Generating error data, in particular an
error message, at the point in the code where the error occurs enables such distinction. We could
repurpose the boolean return value and maintain it as an indication of whether or not the game state
has been modified (we could also simply return `void` instead). The program should now display the
following error messages (notice the two messages, one from each level of exception):

[^3]: If, instead of having the method return false, you checked this and other conditions from
the `execute` method of the `AddPlantCommand`, `AddPlantCheatCommand` or `AddZombieCommand`,
you should now remove these checks in order to use the exceptions policy known as EAFP rather than
that known as LBYL.

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

***To be added shortly***
