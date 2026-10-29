# Práctica 2 (Parte II): Plantas contra zombis _extended_

Antes de comenzar recordad la **advertencia**:

> La falta de encapsulación, el uso de métodos que devuelvan listas, y el uso de `instanceof` o `getClass()` tiene como
> consecuencia un **suspenso directo** en la práctica. Es incluso peor implementar un `instanceof` casero, por ejemplo
> así: cada subclase de la clase `GameObject` contiene un conjunto de métodos `esX`, uno por cada subclase X de
`GameObject`; el método `esX` de la clase X devuelve `true` y los demás métodos `esX` de la clase X devuelven `false`.


**NOTA:** Te recomendamos que leas completamente el enunciado antes de ponerte a implementar la funcionalidad
solicitada. Primero explicamos las nuevas funcionalidades que solicitamos y posteriormente proporcionamos algunos
detalles acerca de cómo puedes implementar estas funcionalidades.

<!-- TOC start -->


- [Práctica 2 (Parte II): Plantas contra zombis Extended](#práctica-2-parte-ii-plantas-contra-zombis-extended)
  - [Extensiones básicas del juego](#extensiones-básicas-del-juego)
    * [Incorporación de nuevos objetos de juego](#incorporación-de-nuevos-objetos-de-juego)
      + [CherryBomb plant](#cherrybomb-plant)
      + [Wall-Nut plant](#wall-nut-plant)
      + [BucketHead zombie](#buckethead-zombie)
      + [Sporty zombie](#sporty-zombie)
      + [Explosive zombie](#explosive-zombie)
    * [Comandos](#comandos)
      + [Comando Reset](#comando-reset)
      + [ListZombiesCommand](#listzombiescommand)
      + [AddPlantCheatCommand](#addplantcheatcommand)
      + [AddZombieCommand](#addzombiecommand)
  - [Cambiando la mecánica para conseguir *suncoins*](#cambiando-la-mecánica-para-conseguir-suncoins)
    * [Detalles de la mecánica](#detalles-de-la-mecánica)
    * [Implementación de la mecánica](#implementación-de-la-mecánica)
  - [La interfaz `GameAction` y las acciones `ExplosionAction`](#la-interfaz-gameaction-y-las-acciones-explosionaction)
  - [Opcional: Pinta la valla y los zombies fuera del tablero](#pinta-zombies-y-valla)
  - [Pruebas](#práctica2-parte2-pruebas)

<!-- TOC end -->

<!-- TOC --><a name="práctica-2-parte-ii-plantas-contra-zombis-extended"></a>
<!-- TOC --><a name="extensiones-básicas-del-juego"></a>

# Extensiones básicas del juego

<!-- TOC --><a name="incorporación-de-nuevos-objetos-de-juego"></a>

## Incorporación de nuevos objetos de juego

Ahora que tenemos nuestra factoría y hemos utilizado herencia para generalizar los objetos de juego va a ser muy
sencillo extender el juego con nuevas plantas y zombis. De esta forma, vamos a incluir las siguientes plantas:

- `CherryBomb`
- `WallNut`

En cuanto a los zombies vamos a añadir los siguientes:

- `BucketHead`
- `Sporty`
- `ExplosiveZombie`

Los tres nuevos zombis ejercen el mismo daño que el zombi común de la primera práctica. Los diferentes tipos de zombis
aparecen con la misma probabilidad.

Para hacer estos cambios organiza tu jerarquía de objetos de juego para que tengan como base `GameObject`, del que heredan
`Plant` y `Zombie`, de los cuales heredan las clases concretas de plantas y zombies. Mientras implementes esta nueva
jerarquía, analiza qué clases de tu jerarquía deberían de ser clases abstractas.

Si has planteado bien la lista de objetos y el juego, no deberías tener que hacer ningún cambio importante, lo único que
tendrás que hacer es crear las clases y registrar los nuevos objetos en sus respectivas factorías.

De todas formas, ten en cuenta que el código es algo vivo, y no sería extraño que a medida que fueses incorporando 
nuevos objetos o funcionalidades al juego te des cuenta de que hay más código que puedes simplificar, escribir de forma
más sencilla o lógica que se mueva de una clase más específica a una más general. No tengas miedo a ir mejorando
tu propio código de forma iterativa. Abraza el cambio :)

Así quedará nuestra lista de plantas.

```
Command > l

[DEBUG] Executing: l

[S]unflower: cost='20' suncoins, damage='0', endurance='1'
[P]eashooter: cost='50' suncoins, damage='1', endurance='3'
[W]all-[N]ut: cost='50' suncoins, damage='0', endurance='10'
[C]herry-Bomb: cost='50' suncoins, damage='10', endurance='2'
```

Así es como veremos la pantalla con los nuevos tipos de objetos.

```
Number of cycles: 20
Sun coins: 130
Remaining zombies: 5
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |     Z[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |              |              |    Bz[08]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |    Sz[02]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |    Ez[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```

<!-- TOC --><a name="cherrybomb-plant"></a>

### CherryBomb plant

Es una planta que explota y quita *10* puntos de daño a todos los zombis que están a su alrededor (a distancia de 1
casilla). Una vez que explota, la planta muere. La explosión ocurre dos ciclos después de ser plantada. Los zombis
también se pueden comer a la planta, que tiene resistencia *2*. Su coste es *50* suncoins.

> NOTA: La planta tiene que estar viva para poder explotar.

Su representación será una `c` minúscula, que pasará a ser mayúscula `C` justo el ciclo anterior a explotar.

<!-- TOC --><a name="wall-nut-plant"></a>

### Wall-Nut plant

Es una planta que sirve como barrera. Tiene coste *50* suncoins y resistencia *10*. Esta planta no produce ningún daño.

Su representación será `WN`.

<!-- TOC --><a name="buckethead-zombie"></a>

### BucketHead zombie

Es un zombie más lento pero más resistente, porque lleva un cubo protegiendo su cabeza. 
Camina un paso cada *4* ciclos y tiene resistencia *8*.

Su representación será `Bz`.

<!-- TOC --><a name="sporty-zombie"></a>

### Sporty zombie

Es un zombie más rápido pero menos resistente. Camina un paso cada ciclo y tiene resistencia *2*.

Su representación será `Sz`.

<!-- TOC --><a name="explosive-zombie"></a>

### Explosive zombie

Tiene la misma velocidad y resistencia que un zombie básico, pero si una planta lo mata explota haciendo *3* de daño a
todas las plantas que tiene a su alrededor.

Su representación será `Ez`.

<!-- TOC --><a name="comandos"></a>

## Comandos

Ahora, vamos a introducir algunos comandos adicionales para *hacer trampas* en el juego, pero que nos permitirán realizar las
pruebas de la aplicación de manera más sencilla.

Al terminar de añadir todos los comandos, si solicitamos la ayuda de la aplicación se deberá generar la siguiente
salida:

```
Command > h

[DEBUG] Executing: h

Available commands:
[a]dd <plant> <col> <row>: add a plant in position (col, row)
[a]dd[Z]ombie <zombie> <col> <row>: add a zombie in position (col, row)
[C]heat[P]lant <plant> <col> <row>: add a plant in position (col, row) without consuming suncoins
[l]ist: print the list of available plants
[l]ist[Z]ombies: print the list of available zombies
[r]eset [<level> <seed>]: start a new game (if level and seed are both provided, they are used to initialize the game)
[h]elp: print this help message
[e]xit: terminate the program
[n]one | "": skips cycle
```


<!-- TOC --><a name="comando-reset"></a>

### Reset

En primer lugar, vamos a modificar ligeramente el comportamiento del comando reset de la Práctica 1 con el objetivo de facilitar las
pruebas, de modo que sea posible cambiar el nivel y la semilla de juego sin tener que parar y volver a arrancar el
juego.

El `ResetCommand` debe ser suficientemente flexible como para o bien recibir 0 parámetros y, por tanto, utilizar el
nivel y semilla que se venían utilizando, o bien recibir 2 parámetros (mismo tipo y orden que
en la línea de comandos) que se utilizarán para reiniciar la partida.

Ten en cuenta que al resetear el juego, también se debe reiniciar la instancia de `Random` que se utiliza para generar
la partida.

Además, otro efecto colateral de este cambio es que la ayuda del comando reset cambia ligeramente (para reflejar que puede
aceptar dos argumentos opcionales).

<!-- TOC --><a name="listzombiescommand"></a>

### ListZombiesCommand

En concreto, el comando `ListZombiesCommand` comando lista los zombies disponibles en el juego.

```
Command > lz

[DEBUG] Executing: lz

Available zombies:
Zombie: speed='2', damage='1', endurance='5'
BucketHead Zombie: speed='4', damage='1', endurance='8'
Sporty Zombie: speed='1', damage='1', endurance='2'
Explosive Zombie: speed='2', damage='1', endurance='5'
```

<!-- TOC --><a name="addplantcheatcommand"></a>

### AddPlantCheatCommand

Este comando es similar a `AddPlantCommand`, es decir, permite añadir una planta al juego, pero en este caso no se
consumen suncoins.

Este comando **hace que se actualice el juego** (al igual que cuando se añade una planta).

<!-- TOC --><a name="addzombiecommand"></a>

### AddZombieCommand

Este comando permite añadir un zombie al juego en una casilla que se encuentre libre dentro del terreno de juego.

Este comando **hace que se actualice el juego** (al igual que cuando se añade una planta).

<!-- TOC --><a name="cambiando-la-mecánica-para-conseguir-suncoins"></a>

# Cambiando la mecánica para conseguir *suncoins*

El siguiente cambio afecta a más partes del juego, por lo que es más delicado. Hasta ahora los `Sunflower`s actualizaban
el número de *suncoins* automáticamente pasado un número de ciclos concretos. Vamos a cambiar esta mecánica para que se
parezca más a la mecánica del juego original, es decir, ahora los `Sunflower`s van a generar *soles* que el jugador va a
tener que recoger. Cuando el jugador recoge un *sol*, este le proporcionará *suncoins*.

Vamos a representar los soles como un nuevo tipo de objeto mediante la clase `Sun`. Y el jugador tendrá que coger los
`Sun`s con el nuevo comando `catch col row`. La lista de comandos disponibles quedará del siguiente modo:

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
[h]elp: print this help message
[e]xit: terminate the program
[n]one | "": skips cycle
```

<!-- TOC --><a name="detalles-de-la-mecánica"></a>

## Detalles de la mecánica

El comportamiento completo de la mecánica de juego es el siguiente:

- Cada `Sunflower` en vez de añadir *suncoins* directamente al jugador cuando pasan 3 ciclos, añadirá un `Sun` en una
  casilla aleatoria.

- Además, aleatoriamente cada *5* ciclos se genera un sun en una posición aleatoria del tablero.

- Puede haber más de un sol en una casilla. Si hay más de uno, sólo se pinta 1.

- Sí puede haber una planta o un zombie en la misma casilla de un sol.

- Los soles estarán disponibles en el juego durante *10* ciclos. Si el usuario no coge los soles, estos desaparecerán.

- El usuario tiene que coger los soles; solo se puede coger soles (si hay más de uno en la misma casilla), una vez por
  turno / ciclo. Cada sol suma *10* suncoins como en la práctica anterior.

> Aclaración: El `GamePrinter` que te proporcionamos como plantilla ya está ajustado para que en cada casilla se puedan
> pintar 1 zombie o 1 planta y 1 sol. Si hay **más de un sol** en una casilla **sólo se pintará 1** pero **se recogerán
todos** con el comando `catch`.

Los soles se representarán con el símbolo `*[XX]` (donde XX son los ciclos que le quedan antes de desaparecer). Como
puede haber una planta o un zombie en la misma casilla de un sol, e incluso los zombies pueden caminar por encima de los
soles, a la hora de pintar el tablero se tendrá que generar de la siguiente manera, concatenando el símbolo del sol con
el del objeto de juego que haya en la casilla:

```
Number of cycles: 28
Sun coins: 10
Remaining zombies: 2
Generated suns: 18
Caught suns: 0

     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |  S[01] *[02] |     *[02]    |              |              |              |  *[06]Sz[02] |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |     S[01]    |              |              |              |              |              |              |     *[05]    |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |     *[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |     *[08]    |              |              |              |              |     *[08]    |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```

<!-- TOC --><a name="implementación-de-la-mecánica"></a>

## Implementación de la mecánica

Para implementar esta parte vamos a proceder con los siguientes cambios:

- Vamos a añadir una clase `Sun`. Recuerda que los soles sólo estarán como mucho *10* ciclos en el juego.

- Los `Sunflowers`, en lugar de actualizar el número de *suncoins*, generarán un nuevo `Sun`.

- Crearemos una clase `SunsManager` que se encargará de centralizar la generación de soles aleatorios. Esta clase es
  similar a `ZombiesManager`. Esta acción sólo se realiza 1 vez por turno / ciclo.

- Actualiza `Game` para delegar en `SunsManager` la generación aleatoria de soles.

- Crearemos el comando `CatchCommand` que delegará en el juego para intentar coger los soles. Recuerda que esta acción
  sólo se puede realizar 1 vez por turno / ciclo.

- Actualiza la información del estado del juego para saber el número de `Sun`s generados y los que el jugador ha cogido.
  Los soles generados son aquellos que se han añadido al juego (se hayan cogido o no) y los soles cogidos son aquellos
  que el usuario ha cogido realmente con el comando `catch col row`

> **NOTA**: Ten en cuenta que al añadir `Sun`s al `GameObjectContainer`, estos van a recibir inmediatamente
`update()`. Ajusta su tiempo de vida para que al añadirlo al juego realmente estén *10* ciclos presentes.

Siguiendo el mismo razonamiento que para los soles, necesitamos algo que nos permita controlar si ya
se ha ejecutado un comando `CatchCommand` en un turno / ciclo concreto.

> NOTA: Este comando *no actualiza* el juego.

El `Game` debe saber si ya se cogieron soles en el ciclo presente, y notificar de alguna forma al `CatchCommand` que no
puede ejecutarse otra vez si ya se cogieron soles.

<!-- TOC --><a name="detalles-de-implementación"></a>

<!-- TOC --><a name="la-interfaz-gameaction-y-las-acciones-explosionaction"></a>

## La interfaz `GameAction` y las acciones `ExplosionAction`

La introducción de los nuevos objetos `CherryBomb` y `ExplosiveZombie` introduce una complicación adicional en el juego
y es que **es necesario gestionar explosiones encadenadas**. Esto es, si una `CherryBomb` explota matando a un
`ExplosiveZombie` este a su vez también explota, por lo que puede matar a alguna planta que tenga a su alcance. A
continuación mostramos el escenario descrito.

```
Number of cycles: 28
Sun coins: 130
Remaining zombies: 5
   
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |              |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |              |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |     C[02]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |     WN[04]   |    Ez[05]    |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```

Para poder implementar este comportamiento tenemos que realizar dos cambios importantes en el juego:

- Eliminar los objetos muertos en `Game#update()` 1 o más veces. Es decir, hay que eliminar objetos muertos mientras
  haya eliminado algún objeto en la pasada anterior.
- Ejecutar acciones de juego como resultado de haber matado / o salir del juego uno de los objetos.

Si no tenemos cuidado a la hora de diseñar e implementar esta funcionalidad, complicaríamos en exceso el código de
`Game` y `GameObjectContainer`. Además, es probable que repartiéramos la responsabilidad de la actualización del juego
entre varias clases.

Para flexibilizar el diseño vamos a introducir la interfaz `GameAction`:

```java
package tp1.pvz.logic.actions;

public interface GameAction {
  void execute(GameWorld game);
}
```

Esta interfaz nos permitirá representar acciones de juego, como una explosión. En nuestro caso tendremos que implementar
la clase `ExplosionAction` que nos permitirá implementar el comportamiento de atacar un zombie o una planta (dependiendo
de qué objeto haya causado la explosión) en las casillas adyacentes a una posición concreta. En el siguiente escenario,
como resultado de explotar la `CherryBomb` de la casilla (5, 2) se ven afectadas las casillas marcadas con **X**. Como
resultado de la explosión, también explotará el `ExplosiveZombie` de (5, 3) afectando a las casillas marcadas con **Y**.

```
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |              |              |              |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |      X       |      X       |      X       |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |      X Y     |     C[02] Y  |      X Y     |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
    |              |              |              |              |   WN[04] X Y |    Ez[05] X  |      X Y     |              |              
     ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── ────────────── 
```

Para lograr este comportamiento vamos a implementar una cola de `GameAction`s que se ejecutaran al comienzo y al final
de cada ciclo del juego. De este modo, cuando un objeto de juego va a explotar, en vez de ejecutarse inmediatamente, se
añade a la cola una instancia de `ExplosionAction` que se ejecutará cuando sea necesario. Como resultado de esa ejecución, 
es posible que otros objetos añadan otros efectos, que a su vez serán ejecutados una vez finalizada la ejecución de los 
efectos ya presentes. Como resultado adicional, la cola de acciones de juego nos permite definir un orden consistente en 
la ejecución de las acciones que nos facilita la depuración de la aplicación.

Ppara ello podemos utilizar cualquier clase que implemente la interfaz `java.util.Deque`. En nuestro caso hemos decidido 
utilizar la clase `java.util.ArrayDeque`.

A continuación mostramos un extracto del código necesario para poder gestionar las `GameAction` dentro de `Game`.

```java
// Game.java

private Deque<GameAction> actions;

// ...

public void reset(Level level, long seed) {
  // ...
  this.actions = new ArrayDeque<>();
}

public void update() {

  // 1. Execute pending actions
  executePendingActions();

  // 2. Execute game Actions

  // 3. Game object updates

  // 4. & 5. Remove dead and execute pending actions
  boolean deadRemoved = true;
  while (deadRemoved || arePendingActionsLeft()) {
    // 4. Remove dead
    deadRemoved = this.container.removeDead();

    // 5. execute pending actions
    executePendingActions();
  }

  this.cycle++;
}

public void pushAction(GameAction gameAction) {
  this.actions.addLast(gameAction);
}

private void executePendingActions() {
  while (!this.actions.isEmpty()) {
    GameAction action = this.actions.removeFirst();
    action.execute(this);
  }
}

private boolean arePendingActionsLeft() {
  return !this.actions.isEmpty();
}
```

<!-- TOC --><a name="pinta-zombies-y-valla"></a>

## Opcional: Pinta la valla y los zombies fuera del tablero

Durante la práctica I no podíamos ver a los zombies que, pese a estar en la partida, aún no habían entrado al tablero.
Queda a vuestra disposición modificar la función `public void showGame()` de `GamePrinter` para lograrlo. A continuación se 
incluye un ejemplo de ejecución con esta modificación estética:

```
Level: EASY
Random generator initialized with seed: 25
Number of cycles: 0
Sun coins: 50
Remaining zombies: 3
Generated suns: 0
Caught suns: 0

   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
   
   ...
   
Number of cycles: 1
Sun coins: 50
Remaining zombies: 2
Generated suns: 0
Caught suns: 0

   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || Bz[08]
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
   
   ...

Number of cycles: 2 - 3
Sun coins: 50
Remaining zombies: 1
Generated suns: 0
Caught suns: 0

   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || Bz[08] Z[05]
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ────────────    
   
   ...
   
Number of cycles: 4
Sun coins: 50
Remaining zombies: 1
Generated suns: 0
Caught suns: 0

   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |    Z[05]   || Bz[08]
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
   
   ...
   
Number of cycles: 10
Sun coins: 50
Remaining zombies: 0
Generated suns: 1
Caught suns: 0

   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |    *[06]   |            |            |            |            |            |            |            || Sz[02]
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |    Z[05]   |            |            |   Bz[08]   || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
  |            |            |            |            |            |            |            |            || 
   ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── ──────────── 
```

<!-- TOC --><a name="práctica2-parte2-pruebas"></a>

## Pruebas

Durante la parte I de la práctica, fruto del refactor que hemos hecho, va a ser necesario:
* Renombrar algunos métodos en las clases de prueba de los objetos del juego (`PeashooterTest`, `SunflowerTest` y 
`ZombieTest`). Basta con hacer un "buscar y reemplazar". Esto es algo normal y natural en el mundo del software: los test,
que no dejan de ser código que acompaña al código principal, evolucionan a medida que lo hace este.
* Deben seguir funcionando igual que cuando se entregó la práctica I. Hemos refactorizado el código (quizá cambiando ligeramente
el nombre de algunos métodos, lo que provoca el renombrado descrito en el punto anterior), pero la funcionalidad no ha cambiado.
Si algún test que no fallaba ahora lo hace, algo hemos _roto_ por el camino.

Ahora bien, en la parte dos sí hemos hecho cambios que afectan a la lógica del juego: hay nuevos comandos, nuevos tipos
de plantas y zombies, etc. Por tanto, es necesario utilizar nuevos test. Estos os los proporcionamos pero tened en cuenta
que no deberíais incorporarlos a vuestro proyecto hasta que la parte I esté completamente finalizada y comencéis a trabajar 
en la parte II.
