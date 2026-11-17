package test;

import org.junit.jupiter.api.BeforeEach;
import pvz.control.Level;
import pvz.control.command.Command;
import pvz.control.exception.GameException;
import pvz.logic.Game;
import pvz.logic.gameobjects.Sun;
import test.RecordingView;
import utils.Position;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Common fixture for command tests: a real {@link Game} (seed 0, EASY) driven through
 * {@link Command#parse(String[])}, and a {@link RecordingView} that remembers what the
 * command asked to show. Commands are given as the user would type them, i.e.
 * {@code <col> <row>}; helper {@link #at(int, int)} takes {@code (row, col)}.
 */
abstract class CommandTestBase {

    protected Game game;
    protected RecordingView view;

    @BeforeEach
    void setUpGameAndView() {
        game = new Game(0L, Level.EASY);
        view = new RecordingView();
    }

    protected static Position at(int row, int col) {
        return new Position(row, col);
    }

    protected Command parse(String... words) {
        try {
            Command command = Command.parse(words);
            assertNotNull(command, "the command must be recognised: " + String.join(" ", words));
            return command;
        } catch (GameException e) {
            throw new AssertionError("the command must be recognised: " + String.join(" ", words), e);
        }
    }

    /** Parses and executes like the {@code Controller} does: a GameException is shown as an error. */
    protected void run(String... words) {
        try {
            Command.parse(words).execute(game, view);
        } catch (GameException e) {
            view.showError(e.getMessage());
        }
    }

    protected String cell(int row, int col) {
        return game.positionToString(at(row, col));
    }

    protected void putSun(int row, int col) {
        game.addGameObject(new Sun(at(row, col), game));
    }
}
