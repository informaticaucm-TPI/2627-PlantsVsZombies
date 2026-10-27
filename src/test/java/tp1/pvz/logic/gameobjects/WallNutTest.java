package tp1.pvz.logic.gameobjects;

import org.junit.jupiter.api.Test;
import tp1.pvz.logic.GameWorld;
import tp1.pvz.utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/** Wall-Nut: a barrier with cost 50, damage 0 and endurance 10, drawn as {@code WN}. */
class WallNutTest extends AbstractPlantTest {

    @Override
    protected Plant create(Position position, GameWorld world) {
        return new WallNut(position, world);
    }

    @Override
    protected String expectedListName() {
        return "[W]all-[N]ut";
    }

    @Override
    protected String expectedSymbol() {
        return "WN";
    }

    @Override
    protected int expectedCost() {
        return 50;
    }

    @Override
    protected int expectedDamage() {
        return 0;
    }

    @Override
    protected int expectedEndurance() {
        return 10;
    }

    @Test
    void cost_constantIs50() {
        assertEquals(50, WallNut.COST);
    }

    @Test
    void name_isWallNut() {
        assertEquals("Wall-Nut", plant.getName());
    }

    @Test
    void update_doesNothing() {
        for (int i = 0; i < 10; i++) {
            plant.update();
        }
        assertTrue(plant.isAlive());
        assertTrue(world.plantAttacks.isEmpty());
        assertTrue(world.zombieAttacks.isEmpty());
        assertTrue(world.pushedActions.isEmpty());
        assertEquals(0, world.generateSunCalls);
    }

    @Test
    void lastsTenTimesLongerThanASunflower() {
        plant.receiveZombieAttack(1);
        assertEquals("WN[09]", plant.getIcon());
    }
}
