package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pvz.logic.actions.ExplosionAction;
import pvz.logic.actions.GameAction;
import test.FakeGameWorld;
import test.FakeGameWorld.Attack;
import utils.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An {@link ExplosionAction} attacks a set of positions: zombies when it comes from
 * a plant, plants when it comes from a zombie.
 */
class ExplosionActionTest {

    private FakeGameWorld world;

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
    }

    @Test
    void plantExplosion_attacksZombiesInEachPosition() {
        GameAction action = new ExplosionAction(10, true, new Position(0, 1), new Position(1, 2));

        action.execute(world);

        assertEquals(List.of(new Attack(new Position(0, 1), 10), new Attack(new Position(1, 2), 10)),
                world.plantAttacks);
        assertTrue(world.zombieAttacks.isEmpty());
    }

    @Test
    void zombieExplosion_attacksPlantsInEachPosition() {
        GameAction action = new ExplosionAction(3, false, new Position(2, 2), new Position(3, 3));

        action.execute(world);

        assertEquals(List.of(new Attack(new Position(2, 2), 3), new Attack(new Position(3, 3), 3)),
                world.zombieAttacks);
        assertTrue(world.plantAttacks.isEmpty());
    }

    @Test
    void withoutPositions_nothingHappens() {
        new ExplosionAction(10, true).execute(world);
        assertTrue(world.plantAttacks.isEmpty());
        assertTrue(world.zombieAttacks.isEmpty());
    }

    @Test
    void executingTwice_attacksTwice() {
        GameAction action = new ExplosionAction(3, false, new Position(0, 0));
        action.execute(world);
        action.execute(world);
        assertEquals(2, world.zombieAttacks.size());
    }

    @Test
    void explosion_canTriggerFurtherActions_throughTheWorld() {
        // an action may push new actions into the world; the game runs them afterwards
        GameAction chained = w -> w.pushAction(new ExplosionAction(1, true, new Position(0, 0)));

        chained.execute(world);

        assertEquals(1, world.pushedActions.size());
        assertInstanceOf(ExplosionAction.class, world.pushedActions.get(0));
    }
}
