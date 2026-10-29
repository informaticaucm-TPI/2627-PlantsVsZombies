package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import utils.Position;
import pvz.view.Messages;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code reset [<level> <seed>]}: with no arguments it restarts the game with the level
 * and seed given on the command line; with two arguments it restarts it with those.
 */
class ResetCommandTest extends CommandTestBase {

    private void playABit() {
        run("add", "s", "0", "0");
        run("cp", "p", "1", "1");
        run("none");
        view = new test.RecordingView();
    }

    private List<Position> next5Random(Game g) {
        List<Position> positions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            positions.add(g.randomPosition());
        }
        return positions;
    }

    // ------------------------------------------------------------------ //
    //  Without arguments                                                   //
    // ------------------------------------------------------------------ //

    @Test
    void reset_restoresTheInitialState() {
        playABit();

        run("reset");

        assertEquals(0, game.getCycles());
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertTrue(game.isEmpty(at(0, 0)));
        assertTrue(game.isEmpty(at(1, 1)));
        assertEquals(3, game.getRemainingZombies());
    }

    @Test
    void reset_redrawsTheBoard() {
        run("r");
        assertEquals(1, view.gamesShown);
        assertTrue(view.errors.isEmpty());
    }

    @Test
    void reset_restartsTheRandomGenerator() {
        List<Position> expected = next5Random(new Game(0L, Level.EASY));
        playABit();

        run("reset");

        assertEquals(expected, next5Random(game));
    }

    @Test
    void reset_clearsTheSunCounters() {
        putSun(0, 0);
        run("c", "0", "0");
        game.generateSun();

        run("reset");

        assertEquals(0, game.getGeneratedCoins());
        assertEquals(0, game.getCaughtCoins());
    }

    // ------------------------------------------------------------------ //
    //  With level and seed                                                 //
    // ------------------------------------------------------------------ //

    @Test
    void resetWithLevelAndSeed_usesThem() {
        playABit();

        run("reset", "HARD", "7");

        assertEquals(0, game.getCycles());
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertTrue(game.isEmpty(at(0, 0)));
        assertEquals(Level.HARD.getNumberOfZombies(), game.getRemainingZombies());
        assertEquals(1, view.gamesShown);
    }

    @Test
    void resetWithLevelAndSeed_reseedsTheRandomGenerator() {
        List<Position> expected = next5Random(new Game(7L, Level.HARD));

        run("reset", "HARD", "7");

        assertEquals(expected, next5Random(game));
    }

    @Test
    void resetWithInsane_hasTenZombies() {
        run("reset", "INSANE", "1");
        assertEquals(10, game.getRemainingZombies());
    }

    
    @Test
    void resetWithLevelAndSeed_acceptsTheLowercaseInputOfTheConsole() {
        // the console lowercases whatever the user types, so "reset HARD 7" reaches the command as "reset hard 7"
        run("reset", "hard", "7");

        assertTrue(view.errors.isEmpty(), "errors: " + view.errors);
        assertEquals(Level.HARD.getNumberOfZombies(), game.getRemainingZombies());
    }

    // ------------------------------------------------------------------ //
    //  Wrong arguments                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void wrongNumberOfArguments_isRejected_andTheGameKeepsGoing() {
        playABit();

        run("reset", "HARD");
        run("reset", "HARD", "7", "1");

        assertEquals(List.of(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER,
                Messages.COMMAND_INCORRECT_PARAMETER_NUMBER), view.errors);
        assertEquals(3, game.getCycles(), "the game was not reset");
        assertEquals(0, view.gamesShown);
    }
}
