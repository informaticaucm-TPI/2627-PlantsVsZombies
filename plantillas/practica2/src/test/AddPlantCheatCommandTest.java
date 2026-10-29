package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.logic.Game;
import test.RecordingView;
import pvz.view.Messages;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code cheatPlant <plant> <col> <row>} (shortcut {@code cp}): like {@code add} but
 * without spending suncoins. It also advances the game one cycle.
 */
class AddPlantCheatCommandTest extends CommandTestBase {

    @Test
    void plant_isPlaced_withoutSpendingSuncoins() {
        run("cheatPlant", "peashooter", "0", "0");
        assertFalse(game.isEmpty(at(0, 0)));
        assertEquals(Game.INITIAL_COINS, game.getCoins());
    }

    @Test
    void everyPlant_isFree() {
        for (String plant : new String[] {"s", "p", "wn", "c"}) {
            setUpGameAndView();
            run("cp", plant, "3", "3");
            assertFalse(game.isEmpty(at(3, 3)), plant);
            assertEquals(Game.INITIAL_COINS, game.getCoins(), plant);
        }
    }

    @Test
    void works_evenWithoutAnySuncoins() {
        run("add", "p", "0", "0"); // coins = 0
        view = new RecordingView();

        run("cp", "wn", "1", "0");

        assertFalse(game.isEmpty(at(0, 1)));
        assertEquals(0, game.getCoins());
        assertTrue(view.errors.isEmpty());
    }

    @Test
    void advancesTheGame_andRedrawsTheBoard() {
        run("cp", "s", "0", "0");
        assertEquals(1, game.getCycles());
        assertEquals(1, view.gamesShown);
    }

    @Test
    void coordinatesAreColumnThenRow() {
        run("cp", "s", "6", "2");
        assertFalse(game.isEmpty(at(2, 6)));
    }

    @Test
    void cheatingSeveralTimes_keepsTheSuncoins() {
        run("cp", "p", "0", "0");
        run("cp", "p", "0", "1");
        run("cp", "p", "0", "2");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(3, game.getCycles());
    }

    
    @Test
    void plant_canBePlantedOnACellWithASun() {
        putSun(2, 2);
        run("cp", "s", "2", "2");
        assertEquals(List.of(), view.errors);
        assertFalse(cell(2, 2).isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Failures                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void unknownPlant_isInvalidObject() {
        run("cp", "tree", "0", "0");
        assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors);
        assertEquals(0, game.getCycles());
        assertEquals(0, view.gamesShown);
    }

    
    @Test
    void aZombieIsNotAPlant() {
        run("cp", "z", "0", "0");
        assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors);
        assertTrue(game.isEmpty(at(0, 0)));
        assertEquals(0, game.getCycles());
    }

    @Test
    void outsideTheBoard_isInvalidPosition() {
        run("cp", "s", "8", "0");
        run("cp", "s", "0", "4");
        assertEquals(List.of(Messages.INVALID_POSITION, Messages.INVALID_POSITION), view.errors);
        assertEquals(0, game.getCycles());
    }

    @Test
    void occupiedCell_isInvalidPosition() {
        run("cp", "s", "0", "0");
        view = new RecordingView();

        run("cp", "p", "0", "0");

        assertEquals(List.of(Messages.INVALID_POSITION), view.errors);
        assertEquals(1, game.getCycles());
        assertEquals(0, view.gamesShown);
    }
}
