package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Plant;
import test.FakeGameWorld;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract shared by every plant. Each concrete plant test supplies the numbers
 * printed by the {@code list} command in the statement.
 */
abstract class AbstractPlantTest {

    protected static final Position POS = new Position(1, 3);

    protected FakeGameWorld world;
    protected Plant plant;

    protected abstract Plant create(Position position, GameWorld world);

    /** Name as printed by {@code list}, e.g. {@code [S]unflower}. */
    protected abstract String expectedListName();

    protected abstract String expectedSymbol();

    protected abstract int expectedCost();

    protected abstract int expectedDamage();

    protected abstract int expectedEndurance();

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
        plant = create(POS, world);
    }

    // ------------------------------------------------------------------ //
    //  Identity                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void initiallyAliveAndInItsPosition() {
        assertTrue(plant.isAlive());
        assertTrue(plant.isInPosition(POS));
        assertFalse(plant.isInPosition(new Position(1, 4)));
        assertFalse(plant.isInPosition(new Position(2, 3)));
    }

    @Test
    void stats_matchStatement() {
        assertEquals(expectedCost(), plant.getCost());
        assertEquals(expectedDamage(), plant.getDamage());
        assertEquals(expectedEndurance(), plant.getEndurance());
    }

    @Test
    void description_matchesListFormat() {
        String expected = "%s: cost='%d' suncoins, damage='%d', endurance='%d'"
                .formatted(expectedListName(), expectedCost(), expectedDamage(), expectedEndurance());
        assertEquals(expected, plant.getDescription());
    }

    @Test
    void icon_showsSymbolAndEndurance() {
        assertEquals("%2s[%02d]".formatted(expectedSymbol(), expectedEndurance()), plant.getIcon());
    }

    // ------------------------------------------------------------------ //
    //  Damage                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void survives_enduranceMinusOneDamage() {
        plant.receiveZombieAttack(expectedEndurance() - 1);
        assertTrue(plant.isAlive());
    }

    @Test
    void dies_whenDamageEqualsEndurance() {
        plant.receiveZombieAttack(expectedEndurance());
        assertFalse(plant.isAlive());
    }

    @Test
    void dies_whenBittenOneByOne() {
        for (int i = 0; i < expectedEndurance() - 1; i++) {
            plant.receiveZombieAttack(1);
            assertTrue(plant.isAlive(), "still alive after " + (i + 1) + " bites");
        }
        plant.receiveZombieAttack(1);
        assertFalse(plant.isAlive());
    }

    @Test
    void zeroDamage_isHarmless() {
        plant.receiveZombieAttack(0);
        assertTrue(plant.isAlive());
    }

    @Test
    void receiveZombieAttack_reportsTheBite() {
        assertTrue(plant.receiveZombieAttack(1));
    }

    @Test
    void peasPassThroughPlants() {
        assertFalse(plant.receivePlantAttack(99));
        assertTrue(plant.isAlive());
    }

    // ------------------------------------------------------------------ //
    //  Life cycle                                                          //
    // ------------------------------------------------------------------ //

    @Test
    void onEnterAndOnExit_doNotTouchTheWorld() {
        plant.onEnter();
        plant.onExit();
        assertTrue(world.pushedActions.isEmpty());
        assertTrue(world.zombieAttacks.isEmpty());
        assertTrue(world.plantAttacks.isEmpty());
        assertEquals(0, world.generateSunCalls);
        assertEquals(0, world.generatedCoins);
    }

    @Test
    void copy_createsAFreshPlantOfTheSameKind() {
        plant.receiveZombieAttack(1);
        Position other = new Position(3, 6);
        Plant copy = plant.copy(other, world);

        assertNotSame(plant, copy);
        assertTrue(copy.isInPosition(other));
        assertTrue(copy.isAlive());
        assertEquals("%2s[%02d]".formatted(expectedSymbol(), expectedEndurance()), copy.getIcon());
        assertEquals(plant.getDescription(), copy.getDescription());
    }

    // ------------------------------------------------------------------ //
    //  Spawn rules                                                         //
    // ------------------------------------------------------------------ //

    @Test
    void canBeSpawned_onAnEmptyBoardCell() {
        assertTrue(plant.canBeSpawnedAt(new Position(0, 0)));
        assertTrue(plant.canBeSpawnedAt(new Position(GameWorld.NUM_ROWS - 1, GameWorld.NUM_COLS - 1)));
    }

    @Test
    void cannotBeSpawned_onAnOccupiedCell() {
        world.occupied.add(new Position(2, 2));
        assertFalse(plant.canBeSpawnedAt(new Position(2, 2)));
    }

    @Test
    void cannotBeSpawned_outsideTheBoard() {
        assertFalse(plant.canBeSpawnedAt(new Position(0, -1)));
        assertFalse(plant.canBeSpawnedAt(new Position(0, GameWorld.NUM_COLS)));
        assertFalse(plant.canBeSpawnedAt(new Position(-1, 0)));
        assertFalse(plant.canBeSpawnedAt(new Position(GameWorld.NUM_ROWS, 0)));
    }
}
