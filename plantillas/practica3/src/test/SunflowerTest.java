package test;

import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Plant;
import pvz.logic.gameobjects.Sunflower;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sunflower: cost 20, damage 0, endurance 1.
 *
 * <p>Since part II it no longer adds suncoins by itself: it asks the world to
 * generate a {@link Sun}, which the player must then catch.
 *
 * <p>Timing: the internal counter starts at 0 and the sun is generated when it
 * reaches 3, so the first sun comes on the 4th update and then one every 3 updates.
 */
class SunflowerTest extends AbstractPlantTest {

    @Override
    protected Plant create(Position position, GameWorld world) {
        return new Sunflower(position, world);
    }

    @Override
    protected String expectedListName() {
        return "[S]unflower";
    }

    @Override
    protected String expectedSymbol() {
        return "S";
    }

    @Override
    protected int expectedCost() {
        return 20;
    }

    @Override
    protected int expectedDamage() {
        return 0;
    }

    @Override
    protected int expectedEndurance() {
        return 1;
    }

    private void updateTimes(int n) {
        for (int i = 0; i < n; i++) {
            plant.update();
        }
    }

    @Test
    void cost_constantIs20() {
        assertEquals(20, Sunflower.COST);
    }

    // ------------------------------------------------------------------ //
    //  Sun generation                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void noSun_before4thUpdate() {
        updateTimes(3);
        assertEquals(0, world.generateSunCalls);
    }

    @Test
    void firstSun_on4thUpdate() {
        updateTimes(4);
        assertEquals(1, world.generateSunCalls);
    }

    @Test
    void afterTheFirstSun_oneSunEvery3Updates() {
        updateTimes(4);
        for (int sun = 2; sun <= 5; sun++) {
            updateTimes(2);
            assertEquals(sun - 1, world.generateSunCalls, "no sun 2 updates after the previous one");
            updateTimes(1);
            assertEquals(sun, world.generateSunCalls, "sun number " + sun + " after 3 updates");
        }
    }

    @Test
    void doesNotAddSuncoinsDirectly() {
        updateTimes(10);
        assertEquals(0, world.generatedCoins,
                "suncoins must only come from catching suns");
    }

    @Test
    void deadSunflower_doesNotGenerateSuns() {
        plant.receiveZombieAttack(1);
        updateTimes(10);
        assertEquals(0, world.generateSunCalls);
    }

    @Test
    void generatingASun_doesNotHurtTheSunflower() {
        updateTimes(8);
        assertTrue(plant.isAlive());
    }
}
