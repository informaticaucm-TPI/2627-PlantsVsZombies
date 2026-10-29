package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.logic.Game;
import test.RecordingView;
import pvz.view.Messages;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code addZombie <zombie> <col> <row>} (shortcut {@code az}): puts a zombie in a free
 * cell of the board, for free. Like adding a plant, it advances the game one cycle.
 */
class AddZombieCommandTest extends CommandTestBase {

    @Test
    void everyKindOfZombie_isPlaced() {
        String[][] zombiesAndIcons = {
            {"z", " Z[05]"}, {"bz", "Bz[08]"}, {"sz", "Sz[02]"}, {"ez", "Ez[05]"}};
        for (String[] zombie : zombiesAndIcons) {
            setUpGameAndView();
            run("az", zombie[0], "7", "2");
            assertEquals(zombie[1], cell(2, 7), zombie[0]);
        }
    }

    @Test
    void zombieNames_alsoWork() {
        run("addZombie", "zombie", "7", "0");
        assertEquals(" Z[05]", cell(0, 7));
    }

    @Test
    void coordinatesAreColumnThenRow() {
        run("az", "z", "6", "3");
        assertEquals(" Z[05]", cell(3, 6));
        assertTrue(game.isEmpty(at(2, 6)));
    }

    @Test
    void doesNotCostSuncoins() {
        run("az", "bz", "7", "0");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
    }

    @Test
    void doesNotConsumeTheLevelQuota() {
        run("az", "z", "7", "0");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(3, game.getRemainingZombies());
    }

    @Test
    void advancesTheGame_andRedrawsTheBoard() {
        run("az", "z", "7", "0");
        assertEquals(1, game.getCycles());
        assertEquals(1, view.gamesShown);
        assertTrue(view.errors.isEmpty());
    }

    
    @Test
    void zombie_canBePlacedOnACellWithASun() {
        putSun(1, 4);
        run("az", "z", "4", "1");
        assertEquals(List.of(), view.errors);
        assertTrue(cell(1, 4).contains("Z["));
    }

    @Test
    void severalZombies_inDifferentCells() {
        run("az", "z", "7", "0");
        run("az", "sz", "7", "1");
        run("az", "ez", "7", "2");
        run("az", "bz", "7", "3");
        assertEquals(4, game.getCycles());
        assertTrue(view.errors.isEmpty());
        for (int row = 0; row < 4; row++) {
            // zombies keep walking while the other ones are added, so look at the whole row
            boolean zombieInRow = false;
            for (int col = 0; col < 8; col++) {
                zombieInRow |= cell(row, col).contains("z[") || cell(row, col).contains("Z[");
            }
            assertTrue(zombieInRow, "row " + row + " should contain a zombie");
        }
    }

    // ------------------------------------------------------------------ //
    //  Failures                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void unknownZombie_isInvalidObject() {
        run("az", "ghost", "7", "0");
        assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors);
        assertEquals(0, game.getCycles());
        assertEquals(0, view.gamesShown);
    }

    
    @Test
    void aPlantIsNotAZombie() {
        for (String plant : new String[] {"s", "p", "wn", "c"}) {
            setUpGameAndView();
            run("az", plant, "3", "3");
            assertEquals(List.of(Messages.INVALID_GAME_OBJECT), view.errors, plant);
            assertTrue(game.isEmpty(at(3, 3)), plant + " must not be placed with addZombie");
            assertEquals(Game.INITIAL_COINS, game.getCoins(), plant + " must not be charged");
            assertEquals(0, game.getCycles(), plant);
        }
    }

    @Test
    void outsideTheBoard_isInvalidPosition() {
        // zombies enter from column 8, but the command only accepts cells of the board
        String[][] positions = {{"8", "0"}, {"0", "4"}, {"-1", "0"}, {"0", "-1"}};
        for (String[] position : positions) {
            setUpGameAndView();
            run("az", "z", position[0], position[1]);
            assertEquals(List.of(Messages.INVALID_POSITION), view.errors, String.join(",", position));
            assertEquals(0, game.getCycles());
            assertEquals(0, view.gamesShown);
        }
    }

    @Test
    void occupiedCell_isInvalidPosition() {
        run("az", "z", "5", "1");
        view = new RecordingView();

        run("az", "bz", "5", "1");

        assertEquals(List.of(Messages.INVALID_POSITION), view.errors);
        assertEquals(1, game.getCycles(), "the second zombie did not advance the game");
        assertEquals(" Z[05]", cell(1, 5), "the first zombie is still alone in the cell");
    }

    @Test
    void cellWithAPlant_isInvalidPosition() {
        run("cp", "wn", "5", "1");
        view = new RecordingView();

        run("az", "z", "5", "1");

        assertEquals(List.of(Messages.INVALID_POSITION), view.errors);
        assertTrue(cell(1, 5).contains("WN["));
        assertFalse(cell(1, 5).contains("Z["));
    }
}
