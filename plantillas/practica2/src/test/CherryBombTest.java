package test;

import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.actions.ExplosionAction;
import pvz.logic.gameobjects.CherryBomb;
import pvz.logic.gameobjects.Plant;
import test.FakeGameWorld.Attack;
import utils.Position;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cherry-Bomb: cost 50, damage 10, endurance 2. Two cycles after being planted it
 * explodes, damaging every zombie at distance 1, and dies. It is drawn as a
 * lowercase {@code c}, which becomes {@code C} the cycle before exploding.
 */
class CherryBombTest extends AbstractPlantTest {

    @Override
    protected Plant create(Position position, GameWorld world) {
        return new CherryBomb(position, world);
    }

    @Override
    protected String expectedListName() {
        return "[C]herry-Bomb";
    }

    @Override
    protected String expectedSymbol() {
        return "c";
    }

    @Override
    protected int expectedCost() {
        return 50;
    }

    @Override
    protected int expectedDamage() {
        return 10;
    }

    @Override
    protected int expectedEndurance() {
        return 2;
    }

    private static Set<Position> neighbours(Position p) {
        return Set.of(p.left(), p.right(), p.up(), p.down(),
                p.upLeft(), p.upRight(), p.downLeft(), p.downRight());
    }

    @Test
    void cost_constantIs50() {
        assertEquals(50, CherryBomb.COST);
    }

    @Test
    void name_isCherryBomb() {
        assertEquals("Cherry-Bomb", plant.getName());
    }

    // ------------------------------------------------------------------ //
    //  Symbol                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void symbol_isLowercaseWhenPlanted() {
        assertEquals("c", plant.getSymbol());
    }

    @Test
    void symbol_becomesUppercaseTheCycleBeforeExploding() {
        plant.update();
        assertEquals("C", plant.getSymbol());
    }

    // ------------------------------------------------------------------ //
    //  Explosion timing                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void doesNotExplode_onTheFirstUpdate() {
        plant.update();
        assertTrue(world.pushedActions.isEmpty());
        assertTrue(plant.isAlive());
    }

    @Test
    void explodes_onTheSecondUpdate() {
        plant.update();
        plant.update();
        assertEquals(1, world.pushedActions.size());
        assertInstanceOf(ExplosionAction.class, world.pushedActions.get(0));
    }

    @Test
    void explosion_isQueued_notExecutedImmediately() {
        plant.update();
        plant.update();
        assertTrue(world.plantAttacks.isEmpty(),
                "the explosion must wait in the action stack until the game runs it");
    }

    @Test
    void dies_afterExploding() {
        plant.update();
        plant.update();
        assertFalse(plant.isAlive());
    }

    // ------------------------------------------------------------------ //
    //  Explosion effect                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void explosion_hits10DamageOnTheEightSurroundingCells() {
        plant.update();
        plant.update();
        world.runPushedActions();

        Set<Position> hit = new HashSet<>();
        for (Attack attack : world.plantAttacks) {
            assertEquals(10, attack.damage());
            hit.add(attack.position());
        }
        assertEquals(neighbours(POS), hit);
        assertEquals(8, world.plantAttacks.size(), "each neighbour is attacked exactly once");
    }

    @Test
    void explosion_doesNotHitItsOwnCell() {
        plant.update();
        plant.update();
        world.runPushedActions();
        for (Attack attack : world.plantAttacks) {
            assertNotEquals(POS, attack.position());
        }
    }

    @Test
    void explosion_neverHurtsPlants() {
        plant.update();
        plant.update();
        world.runPushedActions();
        assertTrue(world.zombieAttacks.isEmpty());
    }

    @Test
    void explosion_nearTheBoardCorner_stillTargetsOffBoardCellsHarmlessly() {
        Plant corner = create(new Position(0, 0), world);
        corner.update();
        corner.update();
        assertDoesNotThrow(world::runPushedActions);
        assertEquals(8, world.plantAttacks.size());
    }

    // ------------------------------------------------------------------ //
    //  The plant must be alive to explode                                  //
    // ------------------------------------------------------------------ //

    @Test
    void eatenBeforeTheFirstUpdate_neverExplodes() {
        plant.receiveZombieAttack(2);
        plant.update();
        plant.update();
        plant.update();
        assertTrue(world.pushedActions.isEmpty());
    }

    @Test
    void eatenBetweenTheTwoUpdates_neverExplodes() {
        plant.update();
        plant.receiveZombieAttack(2);
        plant.update();
        plant.update();
        assertTrue(world.pushedActions.isEmpty());
    }

    @Test
    void explosion_isNotTriggeredByLeavingTheGame() {
        plant.onExit();
        assertTrue(world.pushedActions.isEmpty(),
                "a cherry bomb eaten by a zombie must not explode when it leaves the game");
    }
}
