package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import pvz.control.command.*;
import pvz.view.Messages;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Command#parse(String[])}: which concrete command each user input maps to,
 * how arguments are validated, and what the command table looks like.
 */
class CommandParseTest extends CommandTestBase {

    private void assertParsesTo(Class<? extends Command> expected, String... words) {
        assertInstanceOf(expected, Command.parse(words), String.join(" ", words));
    }

    // ------------------------------------------------------------------ //
    //  Names and shortcuts                                                 //
    // ------------------------------------------------------------------ //

    @Test
    void addPlant_byNameAndShortcut() {
        assertParsesTo(AddPlantCommand.class, "add", "s", "0", "0");
        assertParsesTo(AddPlantCommand.class, "a", "s", "0", "0");
    }

    @Test
    void addZombie_byNameAndShortcut() {
        assertParsesTo(AddZombieCommand.class, "addZombie", "z", "7", "0");
        assertParsesTo(AddZombieCommand.class, "az", "z", "7", "0");
    }

    @Test
    void cheatPlant_byNameAndShortcut() {
        assertParsesTo(AddPlantCheatCommand.class, "cheatPlant", "p", "0", "0");
        assertParsesTo(AddPlantCheatCommand.class, "cp", "p", "0", "0");
    }

    @Test
    void catch_byNameAndShortcut() {
        assertParsesTo(CatchCommand.class, "catch", "0", "0");
        assertParsesTo(CatchCommand.class, "c", "0", "0");
    }

    @Test
    void listPlants_byNameAndShortcut() {
        assertParsesTo(ListPlantsCommand.class, "list");
        assertParsesTo(ListPlantsCommand.class, "l");
    }

    @Test
    void listZombies_byNameAndShortcut() {
        assertParsesTo(ListZombiesCommand.class, "listZombies");
        assertParsesTo(ListZombiesCommand.class, "lz");
    }

    @Test
    void reset_byNameAndShortcut() {
        assertParsesTo(ResetCommand.class, "reset");
        assertParsesTo(ResetCommand.class, "r");
    }

    @Test
    void help_byNameAndShortcut() {
        assertParsesTo(HelpCommand.class, "help");
        assertParsesTo(HelpCommand.class, "h");
    }

    @Test
    void exit_byNameAndShortcut() {
        assertParsesTo(ExitCommand.class, "exit");
        assertParsesTo(ExitCommand.class, "e");
    }

    @Test
    void none_byNameShortcutAndEmptyLine() {
        assertParsesTo(UpdateCommand.class, "none");
        assertParsesTo(UpdateCommand.class, "n");
        assertParsesTo(UpdateCommand.class, "");
    }

    @Test
    void unknownWord_isAnUnknownCommand() {
        assertParsesTo(UnknownCommand.class, "xyzzy");
        assertParsesTo(UnknownCommand.class, "dance", "now");
    }

    @Test
    void unknownCommand_showsTheUnknownCommandError() {
        run("xyzzy");
        assertEquals(java.util.List.of(Messages.UNKNOWN_COMMAND), view.errors);
        assertEquals(0, game.getCycles());
    }
/*  
 * Comentados por defecto porque requieren de cambios en la visibilidad de Command/CommandGenerator
 * 
    @Test
    void everyNameAndShortcut_belongsToASingleCommand() {
        for (Command first : Command.AVAILABLE_COMMANDS) {
            for (Command second : Command.AVAILABLE_COMMANDS) {
                if (first == second) {
                    continue;
                }
                assertFalse(first.matchCommand(second.getName()),
                        first.getName() + " clashes with " + second.getName());
                assertFalse(first.matchCommand(second.getShortcut()),
                        first.getName() + " clashes with " + second.getShortcut());
            }
        }
    }

    @Test
    void everyCommand_hasDetailsAndHelp() {
        for (Command command : Command.AVAILABLE_COMMANDS) {
            assertFalse(command.getDetails().isBlank(), command.getName());
            assertFalse(command.getHelp().isBlank(), command.getName());
        }
    }*/

    // ------------------------------------------------------------------ //
    //  Parameter validation                                                //
    // ------------------------------------------------------------------ //

    @Test
    void commandsWithoutParameters_rejectExtraWords() {
        String[][] inputs = {
            {"list", "x"}, {"lz", "x"}, {"help", "x"}, {"exit", "x"}, {"none", "x"}, {"h", "1", "2"}};
        for (String[] input : inputs) {
            view = new test.RecordingView();
            parse(input).execute(game, view);
            assertEquals(java.util.List.of(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER), view.errors,
                    String.join(" ", input));
        }
        assertEquals(0, game.getCycles());
        assertFalse(game.playerQuits(), "exit with extra words must not quit");
    }

    @Test
    void addCommands_withMissingParameters_reportThem() {
        String[][] inputs = {
            {"add"}, {"add", "s"}, {"add", "s", "1"},
            {"az"}, {"az", "z", "3"},
            {"cp"}, {"cp", "p"}, {"cp", "p", "1"}};
        for (String[] input : inputs) {
            view = new test.RecordingView();
            parse(input).execute(game, view);
            assertEquals(java.util.List.of(Messages.COMMAND_PARAMETERS_MISSING), view.errors,
                    String.join(" ", input));
        }
        assertEquals(0, game.getCycles());
    }

    @Test
    void addCommands_withTooManyParameters_areInvalid() {
        String[][] inputs = {{"add", "s", "1", "1", "1"}, {"az", "z", "1", "1", "1"}, {"cp", "p", "1", "1", "1"}};
        for (String[] input : inputs) {
            view = new test.RecordingView();
            parse(input).execute(game, view);
            assertEquals(java.util.List.of(Messages.INVALID_COMMAND), view.errors, String.join(" ", input));
        }
    }

    @Test
    void catch_withMissingParameters_reportsThem() {
        run("catch");
        run("catch", "1");
        assertEquals(java.util.List.of(Messages.COMMAND_PARAMETERS_MISSING, Messages.COMMAND_PARAMETERS_MISSING),
                view.errors);
    }

    @Test
    void catch_withTooManyParameters_isInvalid() {
        run("catch", "1", "1", "1");
        assertEquals(java.util.List.of(Messages.INVALID_COMMAND), view.errors);
    }

    @Test
    void reset_acceptsZeroOrTwoParameters_only() {
        assertParsesTo(ResetCommand.class, "reset");
        assertParsesTo(ResetCommand.class, "reset", "HARD", "5");

        run("reset", "HARD");
        run("reset", "HARD", "5", "9");
        assertEquals(java.util.List.of(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER,
                Messages.COMMAND_INCORRECT_PARAMETER_NUMBER), view.errors);
    }

    // ------------------------------------------------------------------ //
    //  Simple commands                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void exit_makesThePlayerQuit() {
        run("exit");
        assertTrue(game.playerQuits());
        assertTrue(game.hasGameFinished());
    }

    @Test
    void none_skipsACycle_andRedrawsTheBoard() {
        run("none");
        assertEquals(1, game.getCycles());
        assertEquals(1, view.gamesShown);
    }

    @Test
    void emptyLine_skipsACycle_andRedrawsTheBoard() {
        run("");
        assertEquals(1, game.getCycles());
        assertEquals(1, view.gamesShown);
    }
}
