package test;

import org.junit.jupiter.api.Test;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.SportyZombie;
import pvz.logic.gameobjects.Zombie;
import utils.Position;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sporty zombie: faster (one step per cycle) but weaker (endurance 2). */
class SportyZombieTest extends AbstractZombieTest {

    @Override
    protected Zombie create(Position position, GameWorld world) {
        return new SportyZombie(position, world);
    }

    @Override
    protected String expectedName() {
        return "Sporty Zombie";
    }

    @Override
    protected String expectedSymbol() {
        return "Sz";
    }

    @Override
    protected int expectedSpeed() {
        return 1;
    }

    @Override
    protected int expectedEndurance() {
        return 2;
    }

    @Test
    void walksEveryCycle_afterTheFirstStep() {
        zombie.update();
        assertTrue(zombie.isInPosition(START));
        zombie.update();
        assertTrue(zombie.isInPosition(START.left()));
        zombie.update();
        assertTrue(zombie.isInPosition(START.left().left()));
    }

    @Test
    void diesWithTwoPeaProjectiles() {
        zombie.receivePlantAttack(1);
        assertTrue(zombie.isAlive());
        zombie.receivePlantAttack(1);
        assertFalse(zombie.isAlive());
    }
}
