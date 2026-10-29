package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Explosions through the real game: a cherry bomb explodes two cycles after being
 * planted, and an explosive zombie killed by it explodes in turn (chain reaction).
 *
 * <p>Positions are written as (row, col), the opposite order to the commands.
 * Seed 0 + EASY does not spawn zombies during the first two cycles.
 */
class ExplosionChainTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(0L, Level.EASY);
    }

    private String cell(int row, int col) {
        return game.positionToString(new Position(row, col));
    }

    private void updateTimes(int n) {
        for (int i = 0; i < n; i++) {
            game.update();
        }
    }

    // ------------------------------------------------------------------ //
    //  Cherry bomb alone                                                   //
    // ------------------------------------------------------------------ //

    @Test
    void cherryBomb_isDrawnLowercase_thenUppercaseTheCycleBeforeExploding() {
        game.addGameObjectForFree("c", new Position(2, 5));
        assertTrue(cell(2, 5).contains("c["), "just planted: lowercase c");

        game.update();
        assertTrue(cell(2, 5).contains("C["), "one cycle before exploding: uppercase C");
        assertFalse(cell(2, 5).contains("c["));
    }

    @Test
    void cherryBomb_explodesTwoCyclesAfterBeingPlanted_andDisappears() {
        game.addGameObjectForFree("c", new Position(2, 5));

        game.update();
        assertFalse(game.isEmpty(new Position(2, 5)), "still there after one cycle");

        game.update();
        assertTrue(game.isEmpty(new Position(2, 5)), "gone after exploding");
    }

    @Test
    void cherryBomb_kills_everyAdjacentZombie_includingDiagonals() {
        game.addGameObjectForFree("c", new Position(1, 3));
        // The cell to the right (1, 4) is left out on purpose: a zombie standing there
        // bites the bomb every cycle, which is a different scenario.
        Position[] around = {
            new Position(0, 2), new Position(0, 3), new Position(0, 4),
            new Position(1, 2),
            new Position(2, 2), new Position(2, 3), new Position(2, 4)};
        for (Position p : around) {
            game.addGameObject("bz", p); // the toughest zombie: endurance 8 < damage 10
        }

        updateTimes(2);

        for (Position p : around) {
            assertTrue(game.isEmpty(p), "zombie at " + p + " should have been blown up");
        }
    }

    /** Plants a cherry bomb at (0, 0) and a lone zombie at {@code zombie}; returns the zombie's cell after 2 cycles. */
    private String zombieCellAfterBombExplodes(Position zombie) {
        game.addGameObjectForFree("c", new Position(0, 0));
        game.addGameObject("z", zombie);
        updateTimes(2);
        return game.positionToString(zombie);
    }

    
    @Test
    void cherryBomb_doesNotHurtAZombieTwoCellsAwayInItsRow() {
        // "a todos los zombis que están a su alrededor (a distancia de 1 casilla)"
        assertEquals(" Z[05]", zombieCellAfterBombExplodes(new Position(0, 2)));
    }

    
    @Test
    void cherryBomb_doesNotHurtAZombieFarAwayInItsRow() {
        assertEquals(" Z[05]", zombieCellAfterBombExplodes(new Position(0, 5)));
    }

    @Test
    void cherryBomb_doesNotHurtAZombieTwoCellsAwayInAColumnOrDiagonal() {
        assertEquals(" Z[05]", zombieCellAfterBombExplodes(new Position(2, 2)));
        setUp();
        assertEquals(" Z[05]", zombieCellAfterBombExplodes(new Position(2, 0)));
    }

    @Test
    void cherryBomb_doesNotHurtPlants() {
        game.addGameObjectForFree("c", new Position(2, 5));
        game.addGameObjectForFree("wn", new Position(2, 4));
        game.addGameObjectForFree("s", new Position(1, 5));

        updateTimes(2);

        assertEquals("WN[10]", cell(2, 4));
        assertTrue(cell(1, 5).contains("S[01]"));
    }

    @Test
    void cherryBomb_eatenByAZombieBeforeExploding_doesNotExplode() {
        game.addGameObjectForFree("c", new Position(0, 3));
        game.addGameObject("z", new Position(0, 4));   // would die if the bomb exploded
        game.zombieAttack(new Position(0, 3), 2);      // the bomb (endurance 2) is eaten right now

        updateTimes(2);

        assertEquals(" Z[05]", cell(0, 4), "no explosion without a living cherry bomb");
        assertTrue(game.isEmpty(new Position(0, 3)), "the eaten bomb was removed");
    }

    // ------------------------------------------------------------------ //
    //  Chain reaction (scenario of the statement)                          //
    // ------------------------------------------------------------------ //

    /**
     * <pre>
     *  row 2:                        C (col 5)
     *  row 3:           WN (col 4)   Ez (col 5)
     * </pre>
     */
    private void setUpStatementScenario() {
        game.addGameObjectForFree("c", new Position(2, 5));
        game.addGameObject("ez", new Position(3, 5));
        game.addGameObjectForFree("wn", new Position(3, 4));
    }

    @Test
    void chain_cherryBombKillsTheExplosiveZombie() {
        setUpStatementScenario();
        updateTimes(2);

        assertTrue(game.isEmpty(new Position(3, 5)), "the explosive zombie must die");
        assertTrue(game.isEmpty(new Position(2, 5)), "the cherry bomb must be gone");
    }

    @Test
    void chain_explosiveZombieDamagesThePlantsAroundIt() {
        setUpStatementScenario();
        updateTimes(2);

        String nut = cell(3, 4);
        assertTrue(nut.contains("WN["), "the wall-nut survives");
        int endurance = Integer.parseInt(nut.substring(nut.indexOf('[') + 1, nut.indexOf(']')));
        assertTrue(endurance <= 7, "the wall-nut lost at least the 3 points of the zombie's explosion, was " + nut);
    }

    @Test
    void chain_explosiveZombie_hits3DamageExactly() {
        // the wall-nut is diagonal to the zombie: it does not suffer the zombie's bites
        game.addGameObjectForFree("c", new Position(2, 5));
        game.addGameObject("ez", new Position(3, 5));
        game.addGameObjectForFree("wn", new Position(2, 6));

        updateTimes(2);

        assertEquals("WN[07]", cell(2, 6));
    }

    @Test
    void chain_explosiveZombie_doesNotReachPlantsTwoCellsAway() {
        game.addGameObjectForFree("c", new Position(2, 5));
        game.addGameObject("ez", new Position(3, 5));
        game.addGameObjectForFree("wn", new Position(3, 7));
        game.addGameObjectForFree("wn", new Position(1, 3));

        updateTimes(2);

        assertEquals("WN[10]", cell(3, 7));
        assertEquals("WN[10]", cell(1, 3));
    }

    @Test
    void chain_explosiveZombie_killsFragilePlantsNextToIt() {
        game.addGameObjectForFree("c", new Position(2, 5));
        game.addGameObject("ez", new Position(3, 5));
        game.addGameObjectForFree("s", new Position(2, 6)); // endurance 1 < 3

        updateTimes(2);

        assertTrue(game.isEmpty(new Position(2, 6)));
    }

    @Test
    void chain_explosiveZombieExplosion_doesNotHurtOtherZombies() {
        // bomb (1,3) kills the zombie at (2,2) (its down-left neighbour). The zombie at (2,1)
        // is two cells from the bomb but right next to the zombie that explodes.
        game.addGameObjectForFree("c", new Position(1, 3));
        game.addGameObject("ez", new Position(2, 2));
        game.addGameObject("bz", new Position(2, 1));

        updateTimes(2);

        assertTrue(game.isEmpty(new Position(2, 2)), "the explosive zombie dies");
        assertEquals("Bz[08]", cell(2, 1), "its explosion only hurts plants");
    }

    @Test
    void chain_zombieDeathsAreCounted() {
        setUpStatementScenario();
        int before = game.getRemainingZombies();
        updateTimes(2);
        assertEquals(before, game.getRemainingZombies(),
                "zombies placed by hand do not consume the level quota");
        assertFalse(game.playerWins(), "the quota has not been exhausted");
    }
}
