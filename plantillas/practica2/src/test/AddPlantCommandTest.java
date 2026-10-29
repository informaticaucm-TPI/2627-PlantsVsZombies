package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.logic.Game;
import pvz.view.Messages;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code add <plant> <col> <row>}: buys a plant, then the game advances one cycle
 * and the board is redrawn. Any failure leaves the game untouched.
 */
class AddPlantCommandTest extends CommandTestBase {

    private void assertNothingHappened() {
        assertEquals(0, game.getCycles(), "the cycle must not advance");
        assertEquals(Game.INITIAL_COINS, game.getCoins(), "no suncoins must be spent");
        assertEquals(0, view.gamesShown, "the board must not be redrawn");
    }

    // ------------------------------------------------------------------ //
    //  Success                                                             //
    // ------------------------------------------------------------------ //

    @Test
    void sunflower_costs20() {
        run("add", "sunflower", "0", "0");
        assertEquals(30, game.getCoins());
    }

    @Test
    void peashooter_costs50() {
        run("add", "peashooter", "0", "0");
        assertEquals(0, game.getCoins());
    }

    @Test
    void wallNut_costs50_andShowsAsWN() {
        run("add", "wall-nut", "2", "1");
        assertEquals(0, game.getCoins());
        assertEquals("WN[10]", cell(1, 2));
    }

    @Test
    void cherryBomb_costs50_andShowsAsLowercaseC() {
        run("add", "cherry-bomb", "2", "1");
        assertEquals(0, game.getCoins());
        assertTrue(cell(1, 2).contains("C["), "after the cycle the bomb is one cycle from exploding");
    }

    @Test
    void everyPlant_byItsSymbol() {
        String[] symbols = {"s", "p", "wn", "c"};
        for (String symbol : symbols) {
            setUpGameAndView();
            run("add", symbol, "4", "2");
            assertFalse(game.isEmpty(at(2, 4)), symbol);
            assertEquals(1, game.getCycles(), symbol);
            assertTrue(view.errors.isEmpty(), symbol);
        }
    }

    @Test
    void shortcutA_works() {
        run("a", "s", "0", "0");
        assertEquals(30, game.getCoins());
    }

    @Test
    void coordinatesAreColumnThenRow() {
        run("add", "s", "5", "3");
        assertFalse(game.isEmpty(at(3, 5)), "col 5, row 3");
        assertTrue(game.isEmpty(at(5 % 4, 3)), "not row 5, col 3");
    }

    @Test
    void success_advancesOneCycle_andRedrawsTheBoardOnce() {
        run("add", "s", "0", "0");
        assertEquals(1, game.getCycles());
        assertEquals(1, view.gamesShown);
        assertTrue(view.errors.isEmpty());
    }

    
    @Test
    void plant_canBePlantedOnACellWithASun() {
        // "Sí puede haber una planta o un zombie en la misma casilla de un sol"
        putSun(1, 1);
        run("add", "s", "1", "1");
        assertEquals(List.of(), view.errors);
        assertEquals(30, game.getCoins());
    }

    // ------------------------------------------------------------------ //
    //  Failures                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void unknownPlant_isInvalidObject() {
        run("add", "tree", "0", "0");
        assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors);
        assertNothingHappened();
    }

    
    @Test
    void aZombieIsNotAPlant() {
        for (String zombie : new String[] {"zombie", "z", "bz", "sz", "ez"}) {
            setUpGameAndView();
            run("add", zombie, "0", "0");
            assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors, zombie);
            assertNothingHappened();
            assertTrue(game.isEmpty(at(0, 0)), zombie + " must not be placed with the add command");
        }
    }

    @Test
    void outsideTheBoard_isInvalidPosition() {
        String[][] positions = {{"8", "0"}, {"0", "4"}, {"-1", "0"}, {"0", "-1"}, {"99", "99"}};
        for (String[] position : positions) {
            setUpGameAndView();
            run("add", "s", position[0], position[1]);
            assertEquals(List.of(Messages.INVALID_POSITION), view.errors, String.join(",", position));
            assertNothingHappened();
        }
    }

    @Test
    void occupiedCell_isInvalidPosition_andIsNotCharged() {
        run("add", "s", "0", "0");
        view = new test.RecordingView();

        run("add", "p", "0", "0");

        assertEquals(List.of(Messages.INVALID_POSITION), view.errors);
        assertEquals(30, game.getCoins());
        assertEquals(1, game.getCycles());
        assertEquals(0, view.gamesShown);
    }

    @Test
    void notEnoughSuncoins_showsTheError_andDoesNothing() {
        run("add", "p", "0", "0"); // 50 → 0 coins
        view = new test.RecordingView();

        run("add", "s", "1", "0");

        assertEquals(List.of(Messages.NOT_ENOUGH_COINS), view.errors);
        assertTrue(game.isEmpty(at(0, 1)));
        assertEquals(0, game.getCoins());
        assertEquals(1, game.getCycles(), "only the first plant advanced the game");
        assertEquals(0, view.gamesShown);
    }

    @Test
    void notEnoughSuncoins_forWallNutAndCherryBomb() {
        run("add", "s", "0", "0");
        run("add", "s", "1", "0"); // 10 coins left
        view = new test.RecordingView();

        run("add", "wn", "2", "0");
        run("add", "c", "3", "0");

        assertEquals(List.of(Messages.NOT_ENOUGH_COINS, Messages.NOT_ENOUGH_COINS), view.errors);
        assertEquals(10, game.getCoins());
    }
}
