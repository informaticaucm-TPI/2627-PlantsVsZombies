package test;

import org.junit.jupiter.api.Test;
import pvz.view.Messages;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code catch <col> <row>} (shortcut {@code c}): collects the suns of a cell, giving
 * 10 suncoins each. It does <b>not</b> advance the game and can only succeed once per
 * cycle.
 */
class CatchCommandTest extends CommandTestBase {

    @Test
    void catchingASun_gives10Suncoins() {
        putSun(1, 3);
        run("catch", "3", "1");
        assertEquals(60, game.getCoins());
    }

    @Test
    void shortcutC_works() {
        putSun(0, 0);
        run("c", "0", "0");
        assertEquals(60, game.getCoins());
    }

    @Test
    void coordinatesAreColumnThenRow() {
        putSun(2, 6);
        run("c", "6", "2");
        assertEquals(60, game.getCoins());
    }

    @Test
    void doesNotAdvanceTheGame() {
        putSun(1, 3);
        run("c", "3", "1");
        assertEquals(0, game.getCycles());
    }

    @Test
    void success_redrawsTheBoard_withoutErrors() {
        putSun(1, 3);
        run("c", "3", "1");
        assertEquals(1, view.gamesShown);
        assertTrue(view.errors.isEmpty());
    }

    @Test
    void success_updatesTheCaughtSunsCounter() {
        putSun(1, 3);
        run("c", "3", "1");
        assertEquals(1, game.getCaughtCoins());
    }

    @Test
    void success_removesTheSunFromTheBoard() {
        putSun(1, 3);
        run("c", "3", "1");
        assertFalse(cell(1, 3).contains("*"));
    }

    @Test
    void everySunInTheCellIsCaught() {
        putSun(1, 3);
        putSun(1, 3);
        run("c", "3", "1");
        assertEquals(70, game.getCoins());
        assertEquals(2, game.getCaughtCoins());
    }

    @Test
    void aSunSharingACellWithAPlant_canBeCaught() {
        game.addGameObjectForFree("s", at(0, 0));
        putSun(0, 0);
        run("c", "0", "0");
        assertEquals(60, game.getCoins());
        assertTrue(cell(0, 0).contains("S["));
    }

    @Test
    void sunGeneratedBySunflower_canBeCaught() {
        game.generateSun();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                if (cell(row, col).contains("*")) {
                    run("c", String.valueOf(col), String.valueOf(row));
                }
            }
        }
        assertEquals(60, game.getCoins());
        assertEquals(1, game.getCaughtCoins());
    }

    // ------------------------------------------------------------------ //
    //  Once per cycle                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void secondCatchInTheSameCycle_fails() {
        putSun(0, 0);
        putSun(3, 7);
        run("c", "0", "0");
        view = new test.RecordingView();

        run("c", "7", "3");

        assertEquals(1, view.errors.size());
        assertEquals(60, game.getCoins());
        assertEquals(0, view.gamesShown);
        assertTrue(cell(3, 7).contains("*"), "the second sun is still there");
    }

    @Test
    void catchIsAvailableAgain_inTheNextCycle() {
        putSun(0, 0);
        putSun(3, 7);
        run("c", "0", "0");
        run("none");

        run("c", "7", "3");

        assertEquals(70, game.getCoins());
        assertEquals(2, game.getCaughtCoins());
    }

    @Test
    void afterAFailedCatch_theCycleCanStillBeUsed() {
        putSun(3, 7);
        run("c", "0", "0"); // nothing there
        run("c", "7", "3");
        assertEquals(60, game.getCoins());
    }

    // ------------------------------------------------------------------ //
    //  Failures                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void cellWithoutSun_showsAnError_andChangesNothing() {
        run("c", "3", "1");
        assertEquals(List.of(Messages.NO_CATCHABLE_IN_POSITION.formatted(3, 1)), view.errors);
        assertEquals(50, game.getCoins());
        assertEquals(0, game.getCycles());
        assertEquals(0, view.gamesShown);
    }

    @Test
    void errorMessage_mentionsColumnBeforeRow() {
        run("c", "6", "2");
        assertEquals("No sun in (6, 2)", view.errors.get(0));
    }

    @Test
    void cellWithAPlantOnly_hasNothingToCatch() {
        game.addGameObjectForFree("s", at(0, 0));
        run("c", "0", "0");
        assertEquals(1, view.errors.size());
        assertEquals(50, game.getCoins());
    }

    @Test
    void outsideTheBoard_isInvalidPosition() {
        String[][] positions = {{"8", "0"}, {"0", "4"}, {"-1", "0"}, {"0", "-1"}};
        for (String[] position : positions) {
            setUpGameAndView();
            run("c", position[0], position[1]);
            assertEquals(List.of(Messages.INVALID_POSITION), view.errors, String.join(",", position));
            assertEquals(0, view.gamesShown);
        }
    }
}
