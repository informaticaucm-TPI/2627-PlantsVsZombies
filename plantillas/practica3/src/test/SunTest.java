package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Sun;
import test.FakeGameWorld;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sun: stays 10 cycles on the board (counting the cycle in which it is generated),
 * is drawn as {@code *[XX]} and gives 10 suncoins when caught.
 */
class SunTest {

    private static final Position POS = new Position(2, 4);

    private FakeGameWorld world;
    private Sun sun;

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
        sun = new Sun(POS, world);
    }

    // ------------------------------------------------------------------ //
    //  Life time                                                           //
    // ------------------------------------------------------------------ //

    @Test
    void initiallyAlive() {
        assertTrue(sun.isAlive());
        assertTrue(sun.isInPosition(POS));
    }

    @Test
    void firstUpdate_happensInTheCycleItIsGenerated_soItShows10() {
        sun.update();
        assertEquals(" *[10]", sun.getIcon());
    }

    @Test
    void icon_countsDownEveryCycle() {
        sun.update();
        sun.update();
        sun.update();
        assertEquals(" *[08]", sun.getIcon());
    }

    @Test
    void staysOnTheBoardFor10Cycles() {
        for (int cycle = 1; cycle <= 10; cycle++) {
            sun.update();
            assertTrue(sun.isAlive(), "the sun should still be there in cycle " + cycle);
        }
        assertEquals(" *[01]", sun.getIcon());
    }

    @Test
    void disappears_whenItsTenCyclesAreOver() {
        for (int cycle = 1; cycle <= 11; cycle++) {
            sun.update();
        }
        assertFalse(sun.isAlive());
    }

    // ------------------------------------------------------------------ //
    //  Catching                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void catch_inItsPosition_gives10Suncoins() {
        assertTrue(sun.catchObject(POS));
        assertEquals(10, world.generatedCoins);
    }

    @Test
    void catch_removesTheSunFromTheGame() {
        sun.catchObject(POS);
        assertFalse(sun.isAlive());
    }

    @Test
    void catch_inAnotherPosition_doesNothing() {
        assertFalse(sun.catchObject(new Position(2, 5)));
        assertFalse(sun.catchObject(new Position(3, 4)));
        assertEquals(0, world.generatedCoins);
        assertTrue(sun.isAlive());
    }

    // ------------------------------------------------------------------ //
    //  Interaction with other objects                                      //
    // ------------------------------------------------------------------ //

    @Test
    void zombiesAndPeasPassThroughSuns() {
        assertFalse(sun.receiveZombieAttack(99));
        assertFalse(sun.receivePlantAttack(99));
        assertTrue(sun.isAlive());
    }

    @Test
    void canBeSpawned_onAnOccupiedCell_becausePlantsAndZombiesCanShareACellWithSuns() {
        world.occupied.add(new Position(1, 1));
        assertTrue(sun.canBeSpawnedAt(new Position(1, 1)));
    }

    @Test
    void canBeSpawned_severalSunsInTheSameCell() {
        Sun other = new Sun(POS, world);
        assertTrue(other.canBeSpawnedAt(POS));
    }

    @Test
    void cannotBeSpawned_outsideTheBoard() {
        assertFalse(sun.canBeSpawnedAt(new Position(0, -1)));
        assertFalse(sun.canBeSpawnedAt(new Position(0, GameWorld.NUM_COLS)));
        assertFalse(sun.canBeSpawnedAt(new Position(GameWorld.NUM_ROWS, 0)));
    }
}
