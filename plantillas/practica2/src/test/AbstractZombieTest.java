package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Zombie;
import test.FakeGameWorld.Attack;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract shared by every kind of zombie (Practice 2, part II). Each concrete
 * zombie test only provides the numbers from the statement.
 *
 * <p>Movement timing: the internal counter starts at 0 and a zombie moves when the
 * counter equals its speed, so the first step needs {@code speed + 1} updates and
 * every following step comes {@code speed} updates later.
 */
abstract class AbstractZombieTest {

    protected static final Position START = new Position(0, 5);

    protected FakeGameWorld world;
    protected Zombie zombie;

    protected abstract Zombie create(Position position, GameWorld world);

    /** Name shown by the {@code listZombies} command. */
    protected abstract String expectedName();

    protected abstract String expectedSymbol();

    /** Cycles needed to walk one cell. */
    protected abstract int expectedSpeed();

    protected abstract int expectedEndurance();

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
        zombie = create(START, world);
    }

    // ------------------------------------------------------------------ //
    //  Identity                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void initiallyAliveAndAtStartPosition() {
        assertTrue(zombie.isAlive());
        assertTrue(zombie.isInPosition(START));
        assertFalse(zombie.isInPosition(new Position(0, 4)));
    }

    @Test
    void name_andSymbol_matchStatement() {
        assertEquals(expectedName(), zombie.getName());
        assertEquals(expectedSymbol(), zombie.getSymbol());
    }

    @Test
    void description_matchesListZombiesFormat() {
        String expected = "%s: speed='%d', damage='1', endurance='%d'"
                .formatted(expectedName(), expectedSpeed(), expectedEndurance());
        assertEquals(expected, zombie.getDescription());
    }

    @Test
    void icon_showsSymbolAndRemainingEndurance() {
        assertEquals("%2s[%02d]".formatted(expectedSymbol(), expectedEndurance()), zombie.getIcon());
    }

    @Test
    void icon_reflectsDamageTaken() {
        zombie.receivePlantAttack(1);
        assertEquals("%2s[%02d]".formatted(expectedSymbol(), expectedEndurance() - 1), zombie.getIcon());
    }

    // ------------------------------------------------------------------ //
    //  Damage                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void survives_enduranceMinusOneDamage() {
        zombie.receivePlantAttack(expectedEndurance() - 1);
        assertTrue(zombie.isAlive());
    }

    @Test
    void dies_whenDamageEqualsEndurance() {
        zombie.receivePlantAttack(expectedEndurance());
        assertFalse(zombie.isAlive());
    }

    @Test
    void dies_whenDamageIsAppliedOneByOne() {
        for (int i = 0; i < expectedEndurance() - 1; i++) {
            zombie.receivePlantAttack(1);
            assertTrue(zombie.isAlive(), "still alive after " + (i + 1) + " hits");
        }
        zombie.receivePlantAttack(1);
        assertFalse(zombie.isAlive());
    }

    @Test
    void receivePlantAttack_reportsTheHit() {
        assertTrue(zombie.receivePlantAttack(1));
    }

    @Test
    void zombiesDoNotHurtEachOther() {
        assertFalse(zombie.receiveZombieAttack(99));
        assertTrue(zombie.isAlive());
    }

    // ------------------------------------------------------------------ //
    //  Movement                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void movesOneCellLeft_everySpeedCycles() {
        int speed = expectedSpeed();
        for (int n = 1; n <= 20; n++) {
            zombie.update();
            int steps = n < speed + 1 ? 0 : 1 + (n - (speed + 1)) / speed;
            assertTrue(zombie.isInPosition(new Position(0, 5 - steps)),
                    "after update " + n + " the zombie should have walked " + steps + " cells");
        }
    }

    @Test
    void doesNotMove_whileThePlantInFrontOfItSurvives() {
        world.occupied.add(START.left());
        for (int i = 0; i < expectedSpeed() + 3; i++) {
            zombie.update();
        }
        assertTrue(zombie.isInPosition(START));
    }

    @Test
    void resumesWalking_assoonAsTheWayIsFree() {
        world.occupied.add(START.left());
        for (int i = 0; i < expectedSpeed() + 3; i++) {
            zombie.update();
        }
        world.occupied.clear();

        for (int i = 0; i < expectedSpeed(); i++) {
            zombie.update();
        }
        assertTrue(zombie.isInPosition(START.left()),
                "the zombie should step forward once the cell is free");
    }

    // ------------------------------------------------------------------ //
    //  Attack                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void attacksTheCellOnItsLeft_withDamage1_everyCycleWhileBlocked() {
        world.occupied.add(START.left());
        for (int i = 0; i < 3; i++) {
            zombie.update();
        }
        assertEquals(3, world.zombieAttacks.size());
        for (Attack attack : world.zombieAttacks) {
            assertEquals(new Attack(START.left(), 1), attack);
        }
    }

    @Test
    void neverAttacksPlants_withPlantAttackChannel() {
        world.occupied.add(START.left());
        zombie.update();
        assertTrue(world.plantAttacks.isEmpty());
    }

    @Test
    void deadZombie_doesNothing() {
        zombie.receivePlantAttack(1000);
        world.occupied.add(START.left());
        for (int i = 0; i < expectedSpeed() + 3; i++) {
            zombie.update();
        }
        assertTrue(zombie.isInPosition(START));
        assertTrue(world.zombieAttacks.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Life cycle                                                          //
    // ------------------------------------------------------------------ //

    @Test
    void onExit_notifiesTheWorldOnce() {
        zombie.onExit();
        assertEquals(1, world.zombiesKilled);
    }

    @Test
    void onEnter_doesNotTouchTheWorld() {
        zombie.onEnter();
        assertEquals(0, world.zombiesKilled);
        assertTrue(world.pushedActions.isEmpty());
        assertTrue(world.zombieAttacks.isEmpty());
    }

    @Test
    void copy_createsAFreshZombieOfTheSameKind() {
        zombie.receivePlantAttack(1);
        Position other = new Position(2, 7);
        Zombie copy = zombie.copy(other, world);

        assertNotSame(zombie, copy);
        assertTrue(copy.isInPosition(other));
        assertTrue(copy.isAlive());
        assertEquals("%2s[%02d]".formatted(expectedSymbol(), expectedEndurance()), copy.getIcon(),
                "a copy starts with full endurance");
        assertEquals(zombie.getDescription(), copy.getDescription());
    }

    // ------------------------------------------------------------------ //
    //  Spawn rules                                                         //
    // ------------------------------------------------------------------ //

    @Test
    void canBeSpawned_justOutsideTheRightEdge() {
        assertTrue(zombie.canBeSpawnedAt(new Position(2, GameWorld.NUM_COLS)));
    }

    @Test
    void canBeSpawned_onAnEmptyBoardCell() {
        assertTrue(zombie.canBeSpawnedAt(new Position(1, 3)));
    }

    @Test
    void cannotBeSpawned_outsideTheRows() {
        assertFalse(zombie.canBeSpawnedAt(new Position(-1, 3)));
        assertFalse(zombie.canBeSpawnedAt(new Position(GameWorld.NUM_ROWS, 3)));
    }
}
