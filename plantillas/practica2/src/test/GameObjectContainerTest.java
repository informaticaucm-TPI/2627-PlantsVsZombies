package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import pvz.logic.GameObjectContainer;
import pvz.logic.gameobjects.BucketHeadZombie;
import pvz.logic.gameobjects.CommonZombie;
import pvz.logic.gameobjects.Sun;
import pvz.logic.gameobjects.Sunflower;
import pvz.logic.gameobjects.WallNut;
import pvz.logic.gameobjects.ExplosiveZombie;
import test.FakeGameWorld;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The container is the single store of game objects: it updates them, removes the
 * dead ones (calling their {@code onExit}) and answers position queries, without
 * knowing the concrete class of anything it holds.
 */
class GameObjectContainerTest {

    private FakeGameWorld world;
    private GameObjectContainer container;

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
        container = new GameObjectContainer();
    }

    // ------------------------------------------------------------------ //
    //  Position queries                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void emptyContainer_hasNothingAnywhere() {
        assertTrue(container.isPositionEmpty(new Position(0, 0)));
        assertEquals("", container.positionToString(new Position(0, 0)));
        //assertNull(container.gameItemInPosition(new Position(0, 0)));
        assertFalse(container.anyInColumn(-1));
    }

    @Test
    void addedObject_occupiesItsPosition() {
        container.add(new WallNut(new Position(1, 2), world));
        assertFalse(container.isPositionEmpty(new Position(1, 2)));
        assertTrue(container.isPositionEmpty(new Position(2, 1)));
        //assertNotNull(container.gameItemInPosition(new Position(1, 2)));
    }

    @Test
    void positionToString_returnsTheIconOfTheObject() {
        container.add(new WallNut(new Position(1, 2), world));
        assertEquals("WN[10]", container.positionToString(new Position(1, 2)));
    }

    @Test
    void positionToString_concatenatesAPlantAndASunInTheSameCell() {
        Position p = new Position(0, 0);
        container.add(new Sunflower(p, world));
        container.add(new Sun(p, world));
        assertEquals(" S[01] *[11]", container.positionToString(p));
    }

    @Test
    void positionToString_concatenatesAZombieAndASunInTheSameCell() {
        Position p = new Position(0, 5);
        container.add(new Sun(p, world));
        container.add(new CommonZombie(p, world));
        assertEquals(" *[11] Z[05]", container.positionToString(p));
    }

    @Test
    void severalSunsInTheSameCell_areAllStored_soCatchCanCollectThemAll() {
        // what is *painted* is a view matter (see GamePrinterTest); the container must keep every sun
        Position p = new Position(3, 3);
        container.add(new Sun(p, world));
        container.add(new Sun(p, world));
        assertEquals(2, container.catchObject(p));
    }

    @Test
    void positionToString_hidesDeadObjects() {
        WallNut nut = new WallNut(new Position(1, 2), world);
        container.add(nut);
        nut.receiveZombieAttack(10);
        assertEquals("", container.positionToString(new Position(1, 2)));
    }

    @Test
    void anyInColumn_findsZombiesThatReachedTheHouse() {
        container.add(new CommonZombie(new Position(2, -1), world));
        assertTrue(container.anyInColumn(-1));
        assertFalse(container.anyInColumn(0));
    }

    // ------------------------------------------------------------------ //
    //  update                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void update_updatesEveryObject() {
        Sun first = new Sun(new Position(0, 0), world);
        Sun second = new Sun(new Position(1, 1), world);
        container.add(first);
        container.add(second);

        container.update();

        assertEquals(" *[10]", first.getIcon());
        assertEquals(" *[10]", second.getIcon());
    }

    
    @Test
    void update_toleratesObjectsAddedWhileUpdating() {
        // A sunflower creates a Sun while the container is iterating over its objects
        Position p = new Position(0, 0);
        container.add(new Sunflower(p, world));
        world.onGenerateSun = () -> container.add(new Sun(new Position(1, 1), world));

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 4; i++) {
                container.update();
            }
        }, "adding a sun from a sunflower's update() must not break the container");
        // Assert that objets are present in container
        assertEquals(" S[01]", container.positionToString(new Position(0, 0)));
        assertEquals(" *[10]", container.positionToString(new Position(1, 1)));
    }

    // ------------------------------------------------------------------ //
    //  removeDead                                                          //
    // ------------------------------------------------------------------ //

    @Test
    void removeDead_withNobodyDead_returnsFalse() {
        container.add(new WallNut(new Position(0, 0), world));
        assertFalse(container.removeDead());
        assertFalse(container.isPositionEmpty(new Position(0, 0)));
    }

    @Test
    void removeDead_removesDeadObjects_andReportsIt() {
        WallNut nut = new WallNut(new Position(0, 0), world);
        container.add(nut);
        nut.receiveZombieAttack(10);

        assertTrue(container.removeDead());
        assertTrue(container.isPositionEmpty(new Position(0, 0)));
        assertFalse(container.removeDead(), "a second pass finds nothing else to remove");
    }

    @Test
    void removeDead_keepsTheLivingOnes() {
        WallNut dead = new WallNut(new Position(0, 0), world);
        container.add(dead);
        container.add(new WallNut(new Position(1, 1), world));
        dead.receiveZombieAttack(10);

        container.removeDead();

        assertTrue(container.isPositionEmpty(new Position(0, 0)));
        assertFalse(container.isPositionEmpty(new Position(1, 1)));
    }

    @Test
    void removeDead_callsOnExit_zombiesNotifyTheWorld() {
        CommonZombie zombie = new CommonZombie(new Position(0, 3), world);
        container.add(zombie);
        zombie.receivePlantAttack(5);

        container.removeDead();

        assertEquals(1, world.zombiesKilled);
    }

    @Test
    void removeDead_callsOnExitExactlyOnce() {
        CommonZombie zombie = new CommonZombie(new Position(0, 3), world);
        container.add(zombie);
        zombie.receivePlantAttack(5);

        container.removeDead();
        container.removeDead();

        assertEquals(1, world.zombiesKilled);
    }

    @Test
    void removeDead_aDyingExplosiveZombie_queuesItsExplosion() {
        ExplosiveZombie zombie = new ExplosiveZombie(new Position(0, 3), world);
        container.add(zombie);
        zombie.receivePlantAttack(5);

        container.removeDead();

        assertEquals(1, world.pushedActions.size());
    }

    @Test
    void removeDead_aDyingBucketHeadZombie_doesNotExplode() {
        BucketHeadZombie zombie = new BucketHeadZombie(new Position(0, 3), world);
        container.add(zombie);
        zombie.receivePlantAttack(8);

        container.removeDead();

        assertTrue(world.pushedActions.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  catchObject                                                         //
    // ------------------------------------------------------------------ //

    @Test
    void catchObject_countsTheSunsCaught_andPaysForEachOne() {
        Position p = new Position(1, 1);
        container.add(new Sun(p, world));
        container.add(new Sun(p, world));
        container.add(new Sun(new Position(2, 2), world));

        assertEquals(2, container.catchObject(p));
        assertEquals(20, world.generatedCoins);
    }

    @Test
    void catchObject_ignoresPlantsAndZombies() {
        Position p = new Position(1, 1);
        container.add(new WallNut(p, world));
        container.add(new CommonZombie(p, world));

        assertEquals(0, container.catchObject(p));
        assertEquals(0, world.generatedCoins);
    }

    @Test
    void catchObject_inAnEmptyCell_catchesNothing() {
        assertEquals(0, container.catchObject(new Position(0, 0)));
    }
}
