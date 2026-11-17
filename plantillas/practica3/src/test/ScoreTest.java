package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Score through the real game: 10 points per zombie removed from the game,
 * 20 when the zombie was removed by an explosion.
 *
 * <p>Positions are written as (row, col), the opposite order to the commands.
 * Seed 0 + EASY does not spawn zombies during the first cycles.
 */
class ScoreTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(0L, Level.EASY);
    }

    private void updateTimes(int n) {
        for (int i = 0; i < n; i++) {
            game.update();
        }
    }

    @Test
    void score_startsAtZero() {
        assertEquals(0, game.getPoints());
    }

    @Test
    void zombieKilledByPeashooter_awardsTenPoints() throws Exception {
        game.addPlantForFree("p", new Position(0, 0));
        game.addZombie("z", new Position(0, 4));

        // a common zombie has endurance 5 and a pea deals 1
        updateTimes(5);

        assertEquals(10, game.getPoints());
    }

    @Test
    void zombieAliveOnTheBoard_awardsNothing() throws Exception {
        game.addZombie("z", new Position(0, 4));
        updateTimes(1);
        assertEquals(0, game.getPoints());
    }

    @Test
    void zombieKilledByCherryBomb_awardsTwentyPoints() throws Exception {
        game.addPlantForFree("c", new Position(1, 3));
        game.addZombie("z", new Position(0, 4));

        updateTimes(3);

        assertEquals(20, game.getPoints());
    }

    @Test
    void severalZombiesKilledByTheSameExplosion_awardTwentyEach() throws Exception {
        game.addPlantForFree("c", new Position(1, 3));
        game.addZombie("z", new Position(0, 3));
        game.addZombie("z", new Position(0, 4));
        game.addZombie("z", new Position(2, 4));

        updateTimes(3);

        assertEquals(60, game.getPoints());
    }

    @Test
    void zombieKilledByPeaAndAnotherByExplosion_addUp() throws Exception {
        game.addPlantForFree("p", new Position(0, 0));
        game.addZombie("z", new Position(0, 4));
        game.addPlantForFree("c", new Position(3, 3));
        game.addZombie("z", new Position(2, 4));

        updateTimes(5);

        assertEquals(30, game.getPoints());
    }

    @Test
    void explosiveZombieKilledByCherryBomb_awardsTwentyPoints() throws Exception {
        // its own blast hurts plants, never zombies, so the only points are its own 20
        game.addPlantForFree("c", new Position(1, 3));
        game.addZombie("ez", new Position(0, 4));

        updateTimes(3);

        assertEquals(20, game.getPoints());
    }

    @Test
    void reset_setsTheScoreBackToZero() throws Exception {
        game.addPlantForFree("c", new Position(1, 3));
        game.addZombie("z", new Position(0, 4));
        updateTimes(3);
        assertEquals(20, game.getPoints());

        game.reset();

        assertEquals(0, game.getPoints());
    }

    @Test
    void resetWithParameters_setsTheScoreBackToZero() throws Exception {
        game.addPlantForFree("c", new Position(1, 3));
        game.addZombie("z", new Position(0, 4));
        updateTimes(3);

        game.reset(Level.HARD, 7L);

        assertEquals(0, game.getPoints());
    }
}
