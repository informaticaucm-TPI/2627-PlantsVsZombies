package test;

import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.CommonZombie;
import pvz.logic.gameobjects.Zombie;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The common zombie: speed 2, damage 1, endurance 5. The generic behaviour is in
 * {@link AbstractZombieTest}; the tests below run it against the real {@link Game}.
 */
class CommonZombieTest extends AbstractZombieTest {

    @Override
    protected Zombie create(Position position, GameWorld world) {
        return new CommonZombie(position, world);
    }

    @Override
    protected String expectedName() {
        return "Zombie";
    }

    @Override
    protected String expectedSymbol() {
        return "Z";
    }

    @Override
    protected int expectedSpeed() {
        return 2;
    }

    @Override
    protected int expectedEndurance() {
        return 5;
    }

    // ------------------------------------------------------------------ //
    //  Interaction with the real game                                      //
    // ------------------------------------------------------------------ //

    @Test
    void plantToItsLeft_blocksTheZombie() {
        Game game = new Game(0L, Level.EASY);
        CommonZombie zombie = new CommonZombie(START, game);
        game.addGameObject("wn", new Position(0, 4));

        for (int i = 0; i < 6; i++) {
            zombie.update();
        }
        assertTrue(zombie.isInPosition(START));
    }

    @Test
    void zombie_killsASunflowerOnItsLeft() {
        Game game = new Game(0L, Level.EASY);
        CommonZombie zombie = new CommonZombie(START, game);
        game.addGameObject("s", new Position(0, 4)); // endurance 1

        zombie.update();   // the zombie bites the sunflower
        game.update();     // dead objects are removed at the end of the cycle

        assertTrue(game.isEmpty(new Position(0, 4)),
                "the sunflower should be dead and removed after the zombie's attack");
    }

    @Test
    void zombie_doesNotHurtThePlantsFurtherAway() {
        Game game = new Game(0L, Level.EASY);
        CommonZombie zombie = new CommonZombie(START, game);
        game.addGameObject("s", new Position(0, 2));

        zombie.update();
        game.update();

        assertFalse(game.isEmpty(new Position(0, 2)));
    }
}
