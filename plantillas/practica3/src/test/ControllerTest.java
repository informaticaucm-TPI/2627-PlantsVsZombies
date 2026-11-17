package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import pvz.control.Controller;
import pvz.control.Level;
import pvz.logic.Game;
import pvz.logic.gameobjects.Peashooter;
import pvz.logic.gameobjects.Sun;
import pvz.logic.gameobjects.Sunflower;
import pvz.logic.gameobjects.WallNut;
import utils.Position;
import pvz.view.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests of the game loop: text typed by the user goes through the console
 * view, the command parser and the commands, and the effects are checked on the game
 * and on what was printed.
 *
 * <p>The view lowercases and trims the input, so commands reach the parser lowercase.
 * Each test creates a fresh Game; inputs end with "exit" unless the game ends by itself.
 * System.in / System.out are redirected and restored after each test.
 */
class ControllerTest {

    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    void captureStreams() {
        originalOut = System.out;
        originalIn  = System.in;
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /** Runs the controller with the given input and returns everything written to System.out. */
    private String run(Game game, String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Controller(game).run();

        System.setOut(originalOut);
        System.setIn(originalIn);
        return buffer.toString();
    }

    private Game freshGame() {
        return new Game(0L, Level.EASY);
    }

    private static long boardsDrawn(String output) {
        return output.lines().filter(line -> line.startsWith("Number of cycles:")).count();
    }

    private static boolean mentionsError(String output) {
        String lower = output.toLowerCase();
        return lower.contains("invalid") || lower.contains("error");
    }

    // ------------------------------------------------------------------ //
    //  help / h — does not advance the cycle                              //
    // ------------------------------------------------------------------ //

    @Test
    void helpCommand_longForm_printsHelp() {
        String out = run(freshGame(), "help\nexit\n");
        assertTrue(out.contains("Available commands"));
    }

    @Test
    void helpCommand_shortForm_printsHelp() {
        String out = run(freshGame(), "h\nexit\n");
        assertTrue(out.contains("Available commands"));
    }

    @Test
    void helpCommand_caseInsensitive() {
        String out = run(freshGame(), "Help\nexit\n");
        assertTrue(out.contains("Available commands"));
    }

    @Test
    void helpCommand_listsTheCommandsAddedInPartTwo() {
        String out = run(freshGame(), "h\nexit\n");
        assertTrue(out.contains("[a]dd[Z]ombie <zombie> <col> <row>: add a zombie in position (col, row)"));
        assertTrue(out.contains("[C]heat[P]lant <plant> <col> <row>: add a plant in position (col, row) without consuming suncoins"));
        assertTrue(out.contains("[C]atch <col> <row>: catch a sun, if possible, in position (col, row)"));
        assertTrue(out.contains("[l]ist[Z]ombies: print the list of available zombies"));
        assertTrue(out.contains("[r]eset [<level> <seed>]"));
    }

    @Test
    void helpCommand_doesNotAdvanceCycle() {
        Game game = freshGame();
        run(game, "help\nexit\n");
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  list / l — does not advance the cycle                              //
    // ------------------------------------------------------------------ //

    @Test
    void listCommand_longForm_listsTheFourPlants() {
        String out = run(freshGame(), "list\nexit\n");
        assertTrue(out.contains("[S]unflower: cost='20' suncoins, damage='0', endurance='1'"));
        assertTrue(out.contains("[P]eashooter: cost='50' suncoins, damage='1', endurance='3'"));
        assertTrue(out.contains("[W]all-[N]ut: cost='50' suncoins, damage='0', endurance='10'"));
        assertTrue(out.contains("[C]herry-Bomb: cost='50' suncoins, damage='10', endurance='2'"));
    }

    @Test
    void listCommand_shortForm_listsTheFourPlants() {
        String out = run(freshGame(), "l\nexit\n");
        assertTrue(out.contains("unflower"));
        assertTrue(out.contains("eashooter"));
        assertTrue(out.contains("all-[N]ut"));
        assertTrue(out.contains("herry-Bomb"));
    }

    @Test
    void listCommand_doesNotAdvanceCycle() {
        Game game = freshGame();
        run(game, "list\nexit\n");
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  listZombies / lz                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void listZombies_shortForm_listsTheFourZombies() {
        String out = run(freshGame(), "lz\nexit\n");
        assertTrue(out.contains("Zombie: speed='2', damage='1', endurance='5'"));
        assertTrue(out.contains("BucketHead Zombie: speed='4', damage='1', endurance='8'"));
        assertTrue(out.contains("Sporty Zombie: speed='1', damage='1', endurance='2'"));
        assertTrue(out.contains("Explosive Zombie: speed='2', damage='1', endurance='5'"));
    }

    @Test
    void listZombies_doesNotAdvanceCycle() {
        Game game = freshGame();
        run(game, "lz\nexit\n");
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  none / n / empty — advances the cycle                              //
    // ------------------------------------------------------------------ //

    @Test
    void noneCommand_longForm_advancesCycle() {
        Game game = freshGame();
        run(game, "none\nexit\n");
        assertEquals(1, game.getCycles());
    }

    @Test
    void noneCommand_shortForm_advancesCycle() {
        Game game = freshGame();
        run(game, "n\nexit\n");
        assertEquals(1, game.getCycles());
    }

    @Test
    void emptyInput_advancesCycle() {
        Game game = freshGame();
        run(game, "\nexit\n");
        assertEquals(1, game.getCycles());
    }

    @Test
    void multipleNoneCommands_advancesCyclesCorrectly() {
        Game game = freshGame();
        run(game, "none\nnone\nnone\nexit\n");
        assertEquals(3, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  exit / e                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void exitCommand_endsGame() {
        Game game = freshGame();
        run(game, "exit\n");
        assertTrue(game.playerQuits());
    }

    @Test
    void exitCommand_shortForm_endsGame() {
        Game game = freshGame();
        run(game, "e\n");
        assertTrue(game.playerQuits());
    }

    @Test
    void exitCommand_outputsGameOver_andThatThePlayerLeft() {
        String out = run(freshGame(), "exit\n");
        assertTrue(out.contains(Messages.GAME_OVER));
        assertTrue(out.contains(Messages.PLAYER_QUITS));
    }

    // ------------------------------------------------------------------ //
    //  add — valid plant placement                                         //
    // ------------------------------------------------------------------ //

    @Test
    void addSunflower_byLongName_deductsCoins() {
        Game game = freshGame();
        run(game, "add sunflower 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Sunflower.COST, game.getCoins());
    }

    @Test
    void addSunflower_byShortName_deductsCoins() {
        Game game = freshGame();
        run(game, "add s 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Sunflower.COST, game.getCoins());
    }

    @Test
    void addPeashooter_byLongName_deductsCoins() {
        Game game = freshGame();
        run(game, "add peashooter 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Peashooter.COST, game.getCoins());
    }

    @Test
    void addPeashooter_byShortName_deductsCoins() {
        Game game = freshGame();
        run(game, "add p 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Peashooter.COST, game.getCoins());
    }

    @Test
    void addWallNut_byLongNameAndSymbol_deductsCoins() {
        Game byName = freshGame();
        run(byName, "add wall-nut 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - WallNut.COST, byName.getCoins());
        assertFalse(byName.isEmpty(new Position(0, 0)));

        Game bySymbol = freshGame();
        run(bySymbol, "add wn 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - WallNut.COST, bySymbol.getCoins());
    }

    @Test
    void addCherryBomb_byLongNameAndSymbol_deductsCoins() {
        Game byName = freshGame();
        run(byName, "add cherry-bomb 0 0\nexit\n");
        assertEquals(0, byName.getCoins());

        Game bySymbol = freshGame();
        run(bySymbol, "add c 0 0\nexit\n");
        assertEquals(0, bySymbol.getCoins());
    }

    @Test
    void addCommand_advancesCycle() {
        Game game = freshGame();
        run(game, "add s 0 0\nexit\n");
        assertEquals(1, game.getCycles(), "Successful add must advance the cycle");
    }

    @Test
    void addCommand_shortFormAlias_works() {
        Game game = freshGame();
        run(game, "a s 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Sunflower.COST, game.getCoins());
    }

    @Test
    void addCommand_columnComesBeforeRow() {
        Game game = freshGame();
        run(game, "add s 5 2\nexit\n");
        assertFalse(game.isEmpty(new Position(2, 5)));
    }

    // ------------------------------------------------------------------ //
    //  add — invalid plant type → error, no coin deduction, no cycle      //
    // ------------------------------------------------------------------ //

    @Test
    void addCommand_invalidPlantType_printsError() {
        String out = run(freshGame(), "add tree 0 0\nexit\n");
        assertTrue(mentionsError(out));
    }

    
    @Test
    void addCommand_aZombieIsNotAPlant_printsError() {
        String out = run(freshGame(), "add zombie 0 0\nexit\n");
        assertTrue(mentionsError(out), "a zombie must not be accepted by the add command");
    }

    
    @Test
    void addCommand_aZombieIsNotAPlant_coinsAndCycleUnchanged() {
        Game game = freshGame();
        run(game, "add zombie 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(0, game.getCycles());
        assertTrue(game.isEmpty(new Position(0, 0)));
    }

    @Test
    void addCommand_invalidPlantType_coinsAndCycleUnchanged() {
        Game game = freshGame();
        run(game, "add tree 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  add — invalid / out-of-bounds position → error, no coin deduction  //
    // ------------------------------------------------------------------ //

    @Test
    void addCommand_outOfBoundsPosition_printsError() {
        String out = run(freshGame(), "add s 8 0\nexit\n");
        assertTrue(mentionsError(out));
    }

    @Test
    void addCommand_outOfBoundsPosition_coinsAndCycleUnchanged() {
        Game game = freshGame();
        run(game, "add s 8 0\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(0, game.getCycles());
    }

    @Test
    void addCommand_occupiedPosition_coinsOnlyDeductedOnce() {
        Game game = freshGame();
        run(game, "add s 0 0\nadd s 0 0\nexit\n");
        assertEquals(Game.INITIAL_COINS - Sunflower.COST, game.getCoins());
    }

    // ------------------------------------------------------------------ //
    //  add — missing parameters                                            //
    // ------------------------------------------------------------------ //

    @Test
    void addCommand_missingParameters_printsError() {
        String out = run(freshGame(), "add s\nexit\n");
        assertTrue(out.contains(Messages.COMMAND_PARAMETERS_MISSING));
    }

    @Test
    void addCommand_missingParameters_cycleNotAdvanced() {
        Game game = freshGame();
        run(game, "add s\nexit\n");
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  add — insufficient coins                                            //
    // ------------------------------------------------------------------ //

    @Test
    void addCommand_insufficientCoins_positionRemainsEmpty() {
        Game game = freshGame();
        run(game, "add p 0 0\nadd s 0 1\nexit\n");
        assertEquals(0, game.getCoins());
        assertTrue(game.isEmpty(new Position(1, 0)), "col 0, row 1 must remain empty");
    }

    @Test
    void addCommand_insufficientCoins_onlyTheFirstAddAdvancesTheCycle() {
        Game game = freshGame();
        run(game, "add p 0 0\nadd s 0 1\nexit\n");
        assertEquals(1, game.getCycles());
    }

    @Test
    void addCommand_insufficientCoins_printsError() {
        String out = run(freshGame(), "add p 0 0\nadd s 0 1\nexit\n");
        assertTrue(out.toLowerCase().contains(Messages.NOT_ENOUGH_COINS.toLowerCase()));
    }

    // ------------------------------------------------------------------ //
    //  cheatPlant / cp                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void cheatPlant_placesThePlantForFree_andAdvancesTheCycle() {
        Game game = freshGame();
        run(game, "cp p 3 1\nexit\n");
        assertFalse(game.isEmpty(new Position(1, 3)));
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(1, game.getCycles());
    }

    @Test
    void cheatPlant_canBuyWhatTheCoinsCannotPay() {
        Game game = freshGame();
        run(game, "cp wn 0 0\ncp c 0 1\ncp p 0 2\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(3, game.getCycles());
    }

    
    @Test
    void cheatPlant_longName_worksFromTheConsole() {
        Game game = freshGame();
        run(game, "cheatPlant p 3 1\nexit\n");
        assertFalse(game.isEmpty(new Position(1, 3)),
                "help shows [C]heat[P]lant, so typing cheatPlant must work (the console lowercases input)");
    }

    // ------------------------------------------------------------------ //
    //  addZombie / az                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void addZombie_placesTheZombie_andAdvancesTheCycle() {
        Game game = freshGame();
        String out = run(game, "az bz 6 2\nexit\n");
        assertFalse(game.isEmpty(new Position(2, 6)));
        assertEquals(1, game.getCycles());
        assertTrue(out.contains("Bz[08]"), "the board shows the bucket-head zombie");
    }

    @Test
    void addZombie_invalidCell_printsError() {
        Game game = freshGame();
        String out = run(game, "az z 8 2\nexit\n");
        assertTrue(mentionsError(out));
        assertEquals(0, game.getCycles());
    }

    
    @Test
    void addZombie_longName_worksFromTheConsole() {
        Game game = freshGame();
        run(game, "addZombie z 6 2\nexit\n");
        assertFalse(game.isEmpty(new Position(2, 6)),
                "help shows [a]dd[Z]ombie, so typing addZombie must work (the console lowercases input)");
    }

    // ------------------------------------------------------------------ //
    //  catch / c                                                           //
    // ------------------------------------------------------------------ //

    @Test
    void catch_aSun_paysTenSuncoins_andDoesNotAdvanceTheCycle() {
        Game game = freshGame();
        game.addGameObject(new Sun(new Position(1, 3), game));

        String out = run(game, "catch 3 1\nexit\n");

        assertEquals(Game.INITIAL_COINS + 10, game.getCoins());
        assertEquals(0, game.getCycles());
        assertEquals(2, boardsDrawn(out), "initial board + board after catching");
    }

    @Test
    void catch_shortForm_works() {
        Game game = freshGame();
        game.addGameObject(new Sun(new Position(1, 3), game));
        run(game, "c 3 1\nexit\n");
        assertEquals(Game.INITIAL_COINS + 10, game.getCoins());
    }

    @Test
    void catch_withoutSun_printsError_andDoesNotRedraw() {
        Game game = freshGame();
        String out = run(game, "c 3 1\nexit\n");
        assertTrue(out.contains("No sun in (3, 1)"));
        assertEquals(1, boardsDrawn(out));
        assertEquals(Game.INITIAL_COINS, game.getCoins());
    }

    @Test
    void catch_twiceInTheSameCycle_onlyTheFirstWorks() {
        Game game = freshGame();
        game.addGameObject(new Sun(new Position(0, 0), game));
        game.addGameObject(new Sun(new Position(1, 1), game));

        run(game, "c 0 0\nc 1 1\nexit\n");

        assertEquals(Game.INITIAL_COINS + 10, game.getCoins());
        assertEquals(1, game.getCaughtCoins());
    }

    @Test
    void catch_again_afterTheCycleAdvances() {
        Game game = freshGame();
        game.addGameObject(new Sun(new Position(0, 0), game));
        game.addGameObject(new Sun(new Position(1, 1), game));

        run(game, "c 0 0\nn\nc 1 1\nexit\n");

        assertEquals(Game.INITIAL_COINS + 20, game.getCoins());
        assertEquals(2, game.getCaughtCoins());
    }

    // ------------------------------------------------------------------ //
    //  reset                                                               //
    // ------------------------------------------------------------------ //

    @Test
    void resetCommand_restoresCoinsAndCycles() {
        Game game = freshGame();
        run(game, "add s 0 0\nnone\nreset\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
        assertEquals(0, game.getCycles());
    }

    @Test
    void resetCommand_shortForm_works() {
        Game game = freshGame();
        run(game, "add s 0 0\nr\nexit\n");
        assertEquals(Game.INITIAL_COINS, game.getCoins());
    }

    @Test
    void resetCommand_redrawsTheBoard() {
        String out = run(freshGame(), "reset\nexit\n");
        assertEquals(2, boardsDrawn(out));
    }

    
    @Test
    void resetWithLevelAndSeed_changesTheLevel() {
        Game game = freshGame();
        run(game, "add s 0 0\nreset hard 7\nexit\n");
        assertEquals(Level.HARD.getNumberOfZombies(), game.getRemainingZombies());
        assertEquals(0, game.getCycles());
        assertTrue(game.isEmpty(new Position(0, 0)));
    }

    // ------------------------------------------------------------------ //
    //  Unknown command                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void unknownCommand_printsError() {
        String out = run(freshGame(), "xyzzy\nexit\n");
        assertTrue(out.contains(Messages.UNKNOWN_COMMAND));
    }

    @Test
    void unknownCommand_doesNotAdvanceCycle() {
        Game game = freshGame();
        run(game, "xyzzy\nexit\n");
        assertEquals(0, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  Board is printed only when the game changes                        //
    // ------------------------------------------------------------------ //

    @Test
    void addCommand_onFailure_boardNotReprinted() {
        String out = run(freshGame(), "add tree 0 0\nexit\n");
        assertEquals(1, boardsDrawn(out), "only the initial board");
    }

    @Test
    void addCommand_onSuccess_boardIsReprinted() {
        String out = run(freshGame(), "add s 0 0\nexit\n");
        assertEquals(2, boardsDrawn(out), "initial board + board after the add");
    }

    @Test
    void helpAndLists_doNotReprintTheBoard() {
        String out = run(freshGame(), "h\nl\nlz\nexit\n");
        assertEquals(1, boardsDrawn(out));
    }

    @Test
    void boardHeader_includesGeneratedAndCaughtSuns() {
        String out = run(freshGame(), "exit\n");
        assertTrue(out.contains("Generated suns: 0"));
        assertTrue(out.contains("Caught suns: 0"));
    }

    // ------------------------------------------------------------------ //
    //  The game can end by itself                                          //
    // ------------------------------------------------------------------ //

    @Test
    void zombieReachingTheHouse_endsTheLoop_withoutAnExitCommand() {
        Game game = freshGame();
        // a sporty zombie in column 0 reaches the house on its second update
        String out = run(game, "az sz 0 0\nnone\n");

        assertTrue(game.hasGameFinished());
        assertTrue(out.contains(Messages.GAME_OVER));
        assertTrue(out.contains(Messages.ZOMBIES_WIN));
    }
}
