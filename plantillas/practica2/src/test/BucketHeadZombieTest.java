package test;

import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.BucketHeadZombie;
import pvz.logic.gameobjects.Zombie;
import utils.Position;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** BucketHead zombie: slower (one step every 4 cycles) and tougher (endurance 8). */
class BucketHeadZombieTest extends AbstractZombieTest {

    @Override
    protected Zombie create(Position position, GameWorld world) {
        return new BucketHeadZombie(position, world);
    }

    @Override
    protected String expectedName() {
        return "BucketHead Zombie";
    }

    @Override
    protected String expectedSymbol() {
        return "Bz";
    }

    @Override
    protected int expectedSpeed() {
        return 4;
    }

    @Override
    protected int expectedEndurance() {
        return 8;
    }

    @Test
    void firstStep_needsFiveUpdates() {
        for (int i = 0; i < 4; i++) {
            zombie.update();
            assertTrue(zombie.isInPosition(START));
        }
        zombie.update();
        assertTrue(zombie.isInPosition(START.left()));
    }

    @Test
    void isTougherThanTheCommonZombie() {
        zombie.receivePlantAttack(5); // would kill a common zombie
        assertTrue(zombie.isAlive());
    }
}
