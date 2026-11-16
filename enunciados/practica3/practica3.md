# Práctica 3: Excepciones y ficheros

**Entrega: 30 de noviembre, 09:00**

**Objetivo:** Manejo de excepciones y tratamiento de ficheros.

**Preguntas Frecuentes**: Como es habitual que tengáis dudas (es normal) las iremos recopilando en
este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han
introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2627-PlantsVsZombies-SOLUCION/commits/master/enunciados/faq.md).

## Introducción

En esta práctica se ampliará la funcionalidad del juego en tres aspectos principales:

- Incluir la **definición y el tratamiento de excepciones**. Durante la ejecución del juego pueden presentarse estados
  excepcionales que deben ser tratados de forma particular. Además, cada uno de estos estados debe proporcionar al
  usuario información relevante de por qué se ha llegado a ellos (por ejemplo, errores producidos al procesar un
  determinado comando). El objetivo último es dotar al programa de mayor robustez, así como de mejorar la
  interoperabilidad con el usuario.

- Gestionar una **puntuación asociada a la partida** (los puntos se consiguen al matar zombies) **y records** de puntuación para
  cada nivel del juego que deben perdurar entre partidas.

- **Gestionar ficheros para poder grabar y cargar**, si fuera el caso, los records de partidas anteriores para cada nivel del
  juego.

## Manejo de excepciones

En esta sección se enumerarán las excepciones que deben tratarse durante el juego, se explicará la forma de
implementarlas y se mostrarán ejemplos de ejecución.

### Descripción

El tratamiento de excepciones en un lenguaje como Java resulta muy útil para controlar determinadas situaciones que se
producen durante la ejecución del juego; por ejemplo, para mostrar información relevante al usuario sobre lo ocurrido
durante el parseo o la ejecución de un comando. 

Así, si se quería añadir una planta o zombie al juego, se debía comprobar si la casilla indicada por el usuario estaba
libre y si el jugador disponía de suficientes *suncoins* para añadirla. Si alguna de estas dos condiciones no se daba la
ejecución del comando mostraba un mensaje por pantalla. Pero esto nos obligaba a que el comando "preguntase" al Game, vía GameModel,
por el estado de una celda en concreto. Sí, funciona, cumple su propósito, pero el escenario ideal sería que directamente 
el comando invocase un método en GameModel para tratar de colocar un objeto y si algo va mal, sea este método quién lo
notifique.

En esta práctica vamos a contar con el manejo de excepciones para realizar esta tarea, y ciertos métodos van a poder
lanzar y procesar determinadas excepciones para tratar determinadas situaciones durante el juego. En algunos casos, este
tratamiento consistirá únicamente en proporcionar un mensaje al usuario, mientras que en otros el tratamiento será más
complejo. En esta sección no vamos a ocuparnos de tratar las excepciones relativas a los ficheros, que serán explicadas
en la sección siguiente.

En una primera aproximación vamos a tratar dos tipos de excepciones. Por una parte, se va a definir un nuevo tipo de
excepciones llamado `GameException` que será una superclase que recoge dos nuevos tipos de excepciones:
`CommandParseException` y `CommandExecuteException`. La primera de estas dos sirve para tratar los errores que ocurren
al parsear un comando, es decir, aquellos producidos durante la ejecución del método `parse()`, tales como comando
desconocido, número de parámetros incorrecto y tipo de parámetros no válido. La segunda se utiliza para tratar las
situaciones de error que se pueden dar al ejecutar el método `execute()` de un comando; por ejemplo, no tener
suficientes *suncoins* para ejecutar un comando o que la casilla donde se quiere añadir un elemento del juego esté
ocupada.

Por otra parte, nos ocuparemos de alguna excepción lanzada por el sistema, es decir, no creada ni lanzada por nosotros.
En el juego esto ocurre con la excepción `NumberFormatException`, con la quizá ya os habéis topado anteriormente y que se lanza
cuando se produce un error al tratar de transformar un número entero que se encuentra en formato `String` a su formato
habitual `int`.

### Aspectos generales de la implementación

Una de las principales modificaciones que realizaremos al incluir el manejo de excepciones en el juego consistirá en
ampliar la comunicación entre los comandos y el controlador. De esta forma, la ejecución de un comando (es decir, su método
`execute`) podrá lanzar una excepción para comunicar al `Controller` que algo fue mal.

Básicamente, los cambios que se deben realizar son los siguientes:

- Sólo se imprimirán mensajes de error (usando el método `showMessage`) en la clase `Controller`

- La cabecera del método `create(String[] parameters)` de la clase `Command` pasa a poder lanzar excepciones 
de tipo `CommandParseException`:

```java
public Command create(String[] parameter) throws CommandParseException;
```

- La cabecera del método estático `parse(String[] commandWords)` de `Command` pasa a poder lanzar excepciones de tipo
  `CommandParseException`:

```java
public static Command parse(String[] commandWords) throws CommandParseException;
```

- La cabecera del método abstracto `execute()` de la clase `Command` pasa a poder lanzar excepciones de tipo
  `CommandExecuteException`:

```java
public abstract boolean execute(GameWorld game) throws CommandExecuteException;
```

Así, por ejemplo, el método `parse` lanza una excepción del tipo

```java
throw new CommandParseException(Messages.UNKNOWN_COMMAND);
```

en caso de comando desconocido, en lugar de devolver `null` y esperar a que `Controller` trate el caso mediante un
simple *if-then-else*.

- El controlador debe poder capturar, dentro del método `run()`, las excepciones lanzadas por los dos métodos
  anteriores, que son subclase de una nueva clase de excepciones `GameException`

```java
  public void run() {
		...
  while(!game.isFinished() && !game.isPlayerQuits()) {
			...
    try { ...} catch (GameException ex) { ... }
  }
}
```

- Se deben definir nuevas clases (`GameException`, `CommandParseException`, `CommandExecuteException` y algunas más) y
  lanzar excepciones de estos tipos y tratarlas adecuadamente de forma que el controlador pueda comunicar al usuario los
  problemas que ocurran. Por ejemplo, el método `create()` de la clase `Command` de la práctica
  anterior pasa a ser, aproximadamente, de la siguiente forma:

```java
public Command create(String[] parameters) throws GameException {
  if (parameters.length != 0) {
    throw new CommandParseException(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER);
  }
  return this;
}
```

Haciendo esto así, todos los mensajes se imprimen desde el bucle del método `run()` del controlador, cuyo cuerpo se
parecerá al código siguiente:

```java
while(!game.hasFinished()) {
    view.showGame();
    while (!game.hasGameFinished()) {
      String[] userInput = view.getPrompt();
      try {
        Command command = Command.parse(userInput);
        command.execute(game, view);
      catch (GameException e) {
        view.showError(e.getMessage());
      }
    }
    view.showEndMessage();
}
``` 

En el desarrollo de esta práctica debes definir (en algún caso solo incluir y manejar), lanzar y capturar excepciones
de, **al menos**, los siguientes tipos (en la sección siguiente aparecerá alguno más):

- `GameException`: es la superclase de las excepciones que se deben definir y de la que heredan las subclases
  `CommandParseException` y `CommandExecuteException`.

- `CommandParseException`: nuevo tipo de excepción lanzada por algún error detectado en el parseo de un comando.

  - `NumberFormatException`: excepción del sistema lanzada cuando un elemento proporcionado por el jugador debería ser
    un dato numérico y no lo es. Esta excepción se *recubrirá* en una `CommandParseException`.

- `CommandExecuteException`: nuevo tipo de excepción lanzada por algún error detectado en la ejecución de un comando y
  que no tenga un subtipo de excepción más específico.

- `InvalidPositionException`: nuevo tipo de excepción lanzada cuando una posición del juego proporcionada por el usuario
  está ocupada o no pertenece a una casilla válida. Debería ser consciente de la posición problemática.
  - `NotCatchablePositionException`: Excepción específica para representar una posición sobre la que no se ha podido
    coger ningún objeto (e.g. sol en nuestro caso).

- `NotEnoughSuncoinsException`: nuevo tipo de excepción lanzada cuando no es posible realizar alguna acción pedida por el
  usuario al no tener el jugador suficientes *suncoins* para llevarla a cabo.

- `RecordException`: nuevo tipo de excepción lanzada cuando hay problemas en la lectura o escritura del récord.

Una buena práctica en el tratamiento de excepciones consiste en recoger una excepción de bajo nivel para a continuación
lanzar una de alto nivel que *recubre* la anterior y que contiene su mensaje. 
Por ejemplo, en el comando `AddPlantCommand` podemos hacer lo siguiente:

```java
  } catch(NumberFormatException nfe) {
    throw new CommandParseException(Messages.INVALID_POSITION.formatted(parameters[1], parameters[2]), nfe);
    }
```

De esta forma, conservamos la información de la excepción de bajo nivel, lo cual puede ser relevante para diagnosticar
el origen de dicha excepción.

## Puntuaciones y records de puntuación

Para esta práctica vamos a implementar un pequeño sistema de puntuaciones para el PlantVsZombies. Por cada zombie que
quitemos del juego conseguiremos:

- 10 puntos.
- Si los zombies son **eliminados debido a una explosión** conseguiremos 20 puntos.

La puntuación actual se mostrará durante el juego como parte del estado del mismo:

```
Command > 
[DEBUG] Executing: 

Number of cycles: 55
Sun coins: 180
Remaining zombies: 0
Generated suns: 42
Caught suns: 32
Score: 20
   
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |     S[01]    |  P[03] *[03] |              |              |              |              |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |     S[01]    |              |              |              |              |              |              |     *[07]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |     *[01]    |     P[03]    |     *[05]    |              |              |              |              |     *[10]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |  P[03] *[04] |     *[08]    |              |              |     *[09]    |    Bz[02]    |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 

```

## Guardar y cargar `Record`

La última extensión que vamos a hacer es la funcionalidad de *guardar* y *cargar* el record del juego. El record se
cargará al principio de la partida. Se actualizará cuando la partida se termine (bien porque 
el jugador abandona la partida o porque los zombies ganan o pierden).

Nosotros, por el momento, cargaremos y guardaremos los récords desde un fichero, llamado `record.txt`, presente en 
la raíz del proyecto. Para asegurar que en el futuro los records pueden obtenerse de otras fuentes (un servidor online,
por ejemplo), vamos a crear una abstracción llamada `RecordsManager` (que será una interfaz) y será implementada
por un `FileRecordsManager`. Ambas pertenecen al paquete `pvz.logic.record`.

El `RecordsManager` será responsable de:

- Cargar y salvar los distintos records.
- Durante la inicialización del juego, si el fichero de records no existe o está corrupto 
  se debe lanzar una excepción `RecordException` y terminar el juego.
- Ofrecer la capacidad de consultar el record para un nivel dado.
- Los records no deben almacenarse en ningún orden concreto, y debe soportar cualquier nivel presente o futuro del juego.
El récord se almacena como un entero.
- El fichero podrá permanecer en blanco hasta que se registre la primera puntuación superior a 0 en cualquiera de las dificultades.

> Nota: Si el récord de un nivel no existe, porque no se haya jugado ese nivel, se debe crear un valor por defecto (`0` en este caso).

Veamos el formato del fichero `record.txt`:

```
HARD:20
EASY:20
INSANE:40
```

Para dar a conocer al jugador el récord de su nivel actual vamos a incluir un nuevo comando, `ShowRecordCommand`, usando la letra `o`,
que muestra el récord del nivel actual por pantalla:

```
Command > o

[DEBUG] Executing: o

INSANE record is 30
```

La lista de comandos disponibles en el `help` queda ahora de la siguiente manera:

```
Command > h

[DEBUG] Executing: h

Available commands:
[a]dd <plant> <col> <row>: add a plant in position (col, row)
[a]dd[Z]ombie <zombie> <col> <row>: add a zombie in position (col, row)
[C]heat[P]lant <plant> <col> <row>: add a plant in position (col, row) without consuming suncoins
[C]atch <col> <row>: catch a sun, if possible, in position (col, row)
[l]ist: print the list of available plants
[l]ist[Z]ombies: print the list of available zombies
[r]eset [<level> <seed>]: start a new game (if level and seed are both provided, they are used to initialize the game)
Rec[o]rd: show record of the current level
[h]elp: print this help message
[e]xit: terminate the program
[n]one | "": skips cycle
Command > 
```

La ejecución de este comando no consume un ciclo de ejecución.

## Casos de prueba

Hemos creado y modificado algunos test adaptándolos del siguiente modo:

- Hemos adaptado mínimamente los tests de la práctica anterior a la hora de mostrar las posiciones incorrectas *que
  ahora deben de mostrarse*.
- Hemos añadido algunos tests para verificar que la gestión de los records funciona correctamente.
