package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Sun;
import utils.Position;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Game} through its public API (GameModel, GameWorld and
 * GameStatus), following Practice 1 behaviour plus the part II extensions.
 *
 * <p>Seed 0 + EASY (frequency 0.1): with Random(0) no zombie spawns in the first
 * cycles, which keeps these short scenarios deterministic.
 */
class GameTest {

    private static final long SEED = 0L;
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(SEED, Level.EASY);
    }

    private static Position at(int row, int col) {
        return new Position(row, col);
    }

    private String cell(int row, int col) {
        return game.positionToString(at(row, col));
    }

    /** Number of board cells (inside the board) that currently draw a sun. */
    private int cellsWithSuns() {
        int count = 0;
        for (int row = 0; row < Game.NUM_ROWS; row++) {
            for (int col = 0; col < Game.NUM_COLS; col++) {
                if (cell(row, col).contains("*")) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateTimes(int n) {
        for (int i = 0; i < n; i++) {
            game.update();
        }
    }

    // ------------------------------------------------------------------ //
    //  Initial state                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void initialCoins_are50() {
        assertEquals(50, game.getCoins());
        assertEquals(50, Game.INITIAL_COINS);
    }

    @Test
    void initialCycles_are0() {
        assertEquals(0, game.getCycles());
    }

    @Test
    void initialRemainingZombies_matchLevel() {
        assertEquals(Level.EASY.getNumberOfZombies(), game.getRemainingZombies());
        assertEquals(Level.HARD.getNumberOfZombies(), new Game(SEED, Level.HARD).getRemainingZombies());
        assertEquals(Level.INSANE.getNumberOfZombies(), new Game(SEED, Level.INSANE).getRemainingZombies());
    }

    @Test
    void initialSunStatistics_areZero() {
        assertEquals(0, game.getGeneratedCoins());
        assertEquals(0, game.getCaughtCoins());
    }

    @Test
    void gameNotFinished_initially() {
        assertFalse(game.hasGameFinished());
        assertFalse(game.playerWins());
    }

    @Test
    void playerNotQuitting_initially() {
        assertFalse(game.playerQuits());
    }

    @Test
    void boardIsEmpty_initially() {
        for (int row = 0; row < Game.NUM_ROWS; row++) {
            for (int col = 0; col < Game.NUM_COLS; col++) {
                assertTrue(game.isEmpty(at(row, col)));
            }
        }
    }

    @Test
    void boardDimensions_are4x8() {
        assertEquals(4, GameWorld.NUM_ROWS);
        assertEquals(8, GameWorld.NUM_COLS);
    }

    // ------------------------------------------------------------------ //
    //  Plant placement — cost deduction                                    //
    // ------------------------------------------------------------------ //

    @Test
    void addSunflower_byNameAndBySymbol_costs20() {
        game.addGameObject("sunflower", at(0, 0));
        assertEquals(30, game.getCoins());
        Game other = new Game(SEED, Level.EASY);
        other.addGameObject("s", at(0, 0));
        assertEquals(30, other.getCoins());
    }

    @Test
    void addPeashooter_byNameAndBySymbol_costs50() {
        game.addGameObject("peashooter", at(0, 0));
        assertEquals(0, game.getCoins());
        Game other = new Game(SEED, Level.EASY);
        other.addGameObject("p", at(0, 0));
        assertEquals(0, other.getCoins());
    }

    @Test
    void addWallNut_byNameAndBySymbol_costs50() {
        game.addGameObject("wall-nut", at(0, 0));
        assertEquals(0, game.getCoins());
        Game other = new Game(SEED, Level.EASY);
        other.addGameObject("wn", at(0, 0));
        assertEquals(0, other.getCoins());
    }

    @Test
    void addCherryBomb_byNameAndBySymbol_costs50() {
        game.addGameObject("cherry-bomb", at(0, 0));
        assertEquals(0, game.getCoins());
        Game other = new Game(SEED, Level.EASY);
        other.addGameObject("c", at(0, 0));
        assertEquals(0, other.getCoins());
    }

    @Test
    void addPlant_returnsTrue_whenItIsPlaced() {
        assertTrue(game.addGameObject("s", at(0, 0)));
    }

    // ------------------------------------------------------------------ //
    //  Plant placement — occupancy                                         //
    // ------------------------------------------------------------------ //

    @Test
    void addedPlants_occupyTheirCell() {
        String[] plants = {"s", "p", "wn", "c"};
        for (String plant : plants) {
            Game fresh = new Game(SEED, Level.EASY);
            fresh.addGameObjectForFree(plant, at(1, 2));
            assertFalse(fresh.isEmpty(at(1, 2)), plant);
        }
    }

    @Test
    void emptyPosition_isReportedEmpty() {
        assertTrue(game.isEmpty(at(0, 0)));
    }

    @Test
    void cannotPlantOnAnOccupiedCell() {
        game.addGameObject("s", at(0, 0));
        assertFalse(game.addGameObject("s", at(0, 0)));
        assertEquals(30, game.getCoins(), "the second plant must not be charged");
    }

    // ------------------------------------------------------------------ //
    //  Insufficient coins — plant not placed, coins unchanged              //
    // ------------------------------------------------------------------ //

    @Test
    void insufficientCoins_plantIsNotPlaced_coinsUnchanged() {
        game.addGameObject("p", at(0, 0)); // costs 50 → coins = 0
        assertFalse(game.addGameObject("s", at(0, 1)));
        assertTrue(game.isEmpty(at(0, 1)));
        assertEquals(0, game.getCoins());
    }

    @Test
    void insufficientCoins_forAWallNutAndACherryBomb() {
        game.addGameObject("s", at(0, 0));
        game.addGameObject("s", at(0, 1)); // 10 coins left
        assertFalse(game.addGameObject("wn", at(0, 2)));
        assertFalse(game.addGameObject("c", at(0, 3)));
        assertEquals(10, game.getCoins());
    }

    // ------------------------------------------------------------------ //
    //  Cheating: free plants                                               //
    // ------------------------------------------------------------------ //

    @Test
    void addForFree_doesNotConsumeSuncoins() {
        assertTrue(game.addGameObjectForFree("p", at(0, 0)));
        assertEquals(50, game.getCoins());
    }

    @Test
    void addForFree_worksWithoutSuncoins() {
        game.addGameObject("p", at(3, 3)); // coins = 0
        assertTrue(game.addGameObjectForFree("c", at(0, 0)));
        assertFalse(game.isEmpty(at(0, 0)));
        assertEquals(0, game.getCoins());
    }

    @Test
    void addForFree_onAnOccupiedCell_fails() {
        game.addGameObjectForFree("s", at(0, 0));
        assertFalse(game.addGameObjectForFree("p", at(0, 0)));
    }

    @Test
    void addForFree_unknownName_fails() {
        assertFalse(game.addGameObjectForFree("tree", at(0, 0)));
    }

    // ------------------------------------------------------------------ //
    //  Adding zombies                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void addZombie_ofEveryKind_isPlacedForFree() {
        String[] zombies = {"z", "bz", "sz", "ez"};
        for (String zombie : zombies) {
            Game fresh = new Game(SEED, Level.EASY);
            assertTrue(fresh.addGameObject(zombie, at(2, 5)), zombie);
            assertFalse(fresh.isEmpty(at(2, 5)), zombie);
            assertEquals(50, fresh.getCoins(), "zombies do not cost suncoins: " + zombie);
        }
    }

    @Test
    void addZombie_doesNotConsumeTheLevelQuota() {
        game.addGameObject("z", at(0, 5));
        assertEquals(Level.EASY.getNumberOfZombies(), game.getRemainingZombies());
    }

    @Test
    void addedZombies_showTheirIcon() {
        game.addGameObject("bz", at(0, 5));
        game.addGameObject("sz", at(1, 5));
        game.addGameObject("ez", at(2, 5));
        game.addGameObject("z", at(3, 5));

        assertEquals("Bz[08]", cell(0, 5));
        assertEquals("Sz[02]", cell(1, 5));
        assertEquals("Ez[05]", cell(2, 5));
        assertEquals(" Z[05]", cell(3, 5));
    }

    @Test
    void zombieCanBeAddedJustOutsideTheRightEdge() {
        assertTrue(game.addGameObject("z", at(1, Game.NUM_COLS)));
    }

    // ------------------------------------------------------------------ //
    //  Board geometry                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void isInsideBoard_corners() {
        assertTrue(game.isInsideBoard(at(0, 0)));
        assertTrue(game.isInsideBoard(at(Game.NUM_ROWS - 1, Game.NUM_COLS - 1)));
    }

    @Test
    void isInsideBoard_outsideInEveryDirection() {
        assertFalse(game.isInsideBoard(at(-1, 0)));
        assertFalse(game.isInsideBoard(at(0, -1)));
        assertFalse(game.isInsideBoard(at(Game.NUM_ROWS, 0)));
        assertFalse(game.isInsideBoard(at(0, Game.NUM_COLS)));
    }

    @Test
    void isInBoardLimit_includesTheHouseColumnAndTheSpawnColumn() {
        assertTrue(game.isInBoardLimit(at(0, -1)));
        assertTrue(game.isInBoardLimit(at(0, Game.NUM_COLS)));
        assertTrue(game.isInBoardLimit(at(3, 4)));
    }

    @Test
    void isInBoardLimit_stopsOneColumnBeyondTheBoard() {
        assertFalse(game.isInBoardLimit(at(0, -2)));
        assertFalse(game.isInBoardLimit(at(0, Game.NUM_COLS + 1)));
        assertFalse(game.isInBoardLimit(at(-1, 0)));
        assertFalse(game.isInBoardLimit(at(Game.NUM_ROWS, 0)));
    }

    @Test
    void isValidPosition_needsToBeInsideAndEmpty() {
        assertTrue(game.isValidPosition(at(0, 0)));
        assertFalse(game.isValidPosition(at(0, Game.NUM_COLS)));
        assertFalse(game.isValidPosition(at(-1, 0)));
        game.addGameObject("s", at(0, 0));
        assertFalse(game.isValidPosition(at(0, 0)));
    }

    // ------------------------------------------------------------------ //
    //  update — cycle counter                                              //
    // ------------------------------------------------------------------ //

    @Test
    void update_incrementsCycles() {
        game.update();
        assertEquals(1, game.getCycles());
        game.update();
        assertEquals(2, game.getCycles());
    }

    // ------------------------------------------------------------------ //
    //  Suns: sunflowers                                                    //
    // ------------------------------------------------------------------ //

    
    @Test
    void sunflower_generatesASun_after4Updates() {
        game.addGameObject("s", at(0, 0));
        updateTimes(3);
        assertEquals(0, game.getGeneratedCoins(), "no sun yet after 3 updates");
        updateTimes(1);
        assertEquals(1, game.getGeneratedCoins(), "the sunflower generates its first sun on the 4th update");
        assertTrue(cellsWithSuns() >= 1);
    }

    
    @Test
    void sunflower_doesNotAddSuncoinsByItself() {
        game.addGameObject("s", at(0, 0)); // 30 coins left
        updateTimes(4);
        assertEquals(30, game.getCoins(), "suncoins only arrive when a sun is caught");
    }

    @Test
    void sunflower_generatesSunsPeriodically() {
        game.addGameObject("s", at(0, 0));
        updateTimes(4);
        assertEquals(1, game.getGeneratedCoins());
        updateTimes(3);
        assertEquals(2, game.getGeneratedCoins());
    }

    // ------------------------------------------------------------------ //
    //  Suns: random generation                                             //
    // ------------------------------------------------------------------ //

    @Test
    void randomSun_firstOneAppearsOnTheSixthUpdate() {
        updateTimes(5);
        assertEquals(0, game.getGeneratedCoins());
        updateTimes(1);
        assertEquals(1, game.getGeneratedCoins());
        assertEquals(1, cellsWithSuns());
    }

    @Test
    void randomSun_thenOneEveryFiveUpdates() {
        updateTimes(6);
        updateTimes(4);
        assertEquals(1, game.getGeneratedCoins());
        updateTimes(1);
        assertEquals(2, game.getGeneratedCoins());
    }

    @Test
    void generateSun_countsAsAGeneratedSun_andPutsItOnTheBoard() {
        game.generateSun();
        assertEquals(1, game.getGeneratedCoins());
        assertEquals(1, cellsWithSuns());
    }

    @Test
    void generatedSun_showsItsRemainingCycles() {
        game.generateSun();
        for (int row = 0; row < Game.NUM_ROWS; row++) {
            for (int col = 0; col < Game.NUM_COLS; col++) {
                if (cell(row, col).contains("*")) {
                    assertEquals(" *[11]", cell(row, col));
                }
            }
        }
    }

    @Test
    void sunsDisappear_whenTheirTimeIsOver() {
        Position p = at(2, 2);
        game.addGameObject(new Sun(p, game));

        updateTimes(10);
        assertTrue(cell(2, 2).contains("*[01]"), "the sun is still there during its 10th cycle");

        updateTimes(1);
        assertFalse(cell(2, 2).contains("*[0"), "the sun disappears after 10 cycles");
    }

    
    @Test
    void aPlantCanBePlantedOnACellWithASun() {
        // "Sí puede haber una planta o un zombie en la misma casilla de un sol"
        game.addGameObject(new Sun(at(1, 1), game));

        assertTrue(game.isValidPosition(at(1, 1)));
        assertTrue(game.addGameObject("s", at(1, 1)));
        assertTrue(cell(1, 1).contains("*") && cell(1, 1).contains("S["),
                "the cell draws both the sun and the plant, was: " + cell(1, 1));
    }

    
    @Test
    void aZombieWalksOverSuns() {
        game.addGameObject("sz", at(0, 5));        // moves every cycle after its first step
        game.addGameObject(new Sun(at(0, 4), game));
        game.addGameObject(new Sun(at(0, 3), game));

        updateTimes(3);

        assertTrue(game.isEmpty(at(0, 5)), "the zombie left its cell");
        assertTrue(cell(0, 3).contains("Sz["), "the zombie is now over the second sun: " + cell(0, 3));
    }

    @Test
    void sunsAreNotKilledByZombies() {
        game.addGameObject(new Sun(at(0, 4), game));
        game.zombieAttack(at(0, 4), 5);
        assertTrue(cell(0, 4).contains("*"));
    }

    // ------------------------------------------------------------------ //
    //  Catching suns                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void catch_aSun_gives10Suncoins_andCountsAsCaught() {
        game.addGameObject(new Sun(at(1, 3), game));

        assertTrue(game.catchObject(at(1, 3)));

        assertEquals(60, game.getCoins());
        assertEquals(1, game.getCaughtCoins());
    }

    @Test
    void catch_whereThereIsNoSun_failsWithoutEffects() {
        assertFalse(game.catchObject(at(1, 3)));
        assertEquals(50, game.getCoins());
        assertEquals(0, game.getCaughtCoins());
    }

    @Test
    void catch_doesNotCollectPlantsOrZombies() {
        game.addGameObjectForFree("s", at(0, 0));
        game.addGameObject("z", at(1, 4));
        assertFalse(game.catchObject(at(0, 0)));
        assertFalse(game.catchObject(at(1, 4)));
        assertEquals(50, game.getCoins());
    }

    @Test
    void catch_doesNotAdvanceTheCycle() {
        game.addGameObject(new Sun(at(1, 3), game));
        game.catchObject(at(1, 3));
        assertEquals(0, game.getCycles());
    }

    @Test
    void catch_removesTheSunFromTheBoard() {
        game.addGameObject(new Sun(at(1, 3), game));
        game.catchObject(at(1, 3));
        assertFalse(cell(1, 3).contains("*"), "a caught sun must not be drawn any more");
    }

    @Test
    void catch_collectsEverySunInTheCell() {
        game.addGameObject(new Sun(at(1, 3), game));
        game.addGameObject(new Sun(at(1, 3), game));
        game.addGameObject(new Sun(at(1, 3), game));

        assertTrue(game.catchObject(at(1, 3)));

        assertEquals(80, game.getCoins(), "three suns → 30 suncoins");
        assertEquals(3, game.getCaughtCoins());
    }

    @Test
    void catch_aSunTogetherWithAPlantOrZombie() {
        game.addGameObjectForFree("s", at(0, 0));
        game.addGameObject(new Sun(at(0, 0), game));
        assertTrue(game.catchObject(at(0, 0)));
        assertTrue(cell(0, 0).contains("S["), "the plant stays where it was");
    }

    @Test
    void catch_onlyOncePerCycle() {
        game.addGameObject(new Sun(at(0, 0), game));
        game.addGameObject(new Sun(at(3, 7), game));

        assertTrue(game.catchObject(at(0, 0)));
        assertFalse(game.catchObject(at(3, 7)), "a second catch in the same cycle is not allowed");

        assertEquals(60, game.getCoins());
        assertEquals(1, game.getCaughtCoins());
    }

    @Test
    void catch_isAvailableAgain_afterTheCycleEnds() {
        game.addGameObject(new Sun(at(0, 0), game));
        game.addGameObject(new Sun(at(3, 7), game));
        game.catchObject(at(0, 0));

        game.update();

        assertTrue(game.catchObject(at(3, 7)));
        assertEquals(70, game.getCoins());
        assertEquals(2, game.getCaughtCoins());
    }

    @Test
    void failedCatch_doesNotUseUpTheCycle() {
        game.addGameObject(new Sun(at(3, 7), game));
        assertFalse(game.catchObject(at(0, 0)));
        assertTrue(game.catchObject(at(3, 7)));
    }

    @Test
    void generatedAndCaught_areTrackedSeparately() {
        game.generateSun();
        game.generateSun();
        assertEquals(2, game.getGeneratedCoins());
        assertEquals(0, game.getCaughtCoins());
    }

    // ------------------------------------------------------------------ //
    //  Attacks routed through the game                                     //
    // ------------------------------------------------------------------ //

    @Test
    void plantAttack_hitsTheFirstZombieToTheRight() {
        game.addGameObject("z", at(0, 4));
        game.addGameObject("z", at(0, 6));
        game.addGameObject("p", at(0, 0));
        game.update();

        assertEquals(" Z[04]", cell(0, 4));
        assertEquals(" Z[05]", cell(0, 6));
    }

    @Test
    void plantAttack_doesNothing_whenThereIsNoZombieInTheRow() {
        game.addGameObject("z", at(1, 4));
        assertDoesNotThrow(() -> game.plantAttack(at(0, 1), 1));
        assertEquals(" Z[05]", cell(1, 4));
    }

    @Test
    void plantAttack_startingOffBoard_isHarmless() {
        assertDoesNotThrow(() -> game.plantAttack(at(-1, -1), 10));
        assertDoesNotThrow(() -> game.plantAttack(at(Game.NUM_ROWS, 0), 10));
    }

    @Test
    void zombieAttack_hurtsThePlantInThatCell() {
        game.addGameObjectForFree("wn", at(2, 2));
        game.zombieAttack(at(2, 2), 3);
        assertEquals("WN[07]", cell(2, 2));
    }

    @Test
    void zombieAttack_doesNotHurtOtherZombies() {
        game.addGameObject("z", at(2, 2));
        game.zombieAttack(at(2, 2), 3);
        assertEquals(" Z[05]", cell(2, 2));
    }

    @Test
    void zombieAttack_onAnEmptyCell_doesNothing() {
        assertDoesNotThrow(() -> game.zombieAttack(at(2, 2), 3));
        assertDoesNotThrow(() -> game.zombieAttack(at(-4, 99), 3));
    }

    // ------------------------------------------------------------------ //
    //  Game actions stack                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void pushedAction_isExecutedDuringTheNextUpdate_notBefore() {
        int[] executed = {0};
        game.pushAction(world -> executed[0]++);

        assertEquals(0, executed[0]);
        game.update();
        assertEquals(1, executed[0]);
        game.update();
        assertEquals(1, executed[0], "an action runs only once");
    }

    @Test
    void pushedActions_runLastInFirstOut() {
        List<String> order = new ArrayList<>();
        game.pushAction(world -> order.add("first"));
        game.pushAction(world -> order.add("second"));

        game.update();

        assertEquals(List.of("second", "first"), order);
    }

    @Test
    void actionsPushedByAnAction_runInTheSameUpdate() {
        List<String> order = new ArrayList<>();
        game.pushAction(world -> {
            order.add("outer");
            world.pushAction(w -> order.add("chained"));
        });

        game.update();

        assertEquals(List.of("outer", "chained"), order);
    }

    // ------------------------------------------------------------------ //
    //  Game end                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void zombieReachingTheHouse_endsTheGame_andTheZombiesWin() {
        game.addGameObject("sz", at(0, 0)); // sporty: walks every cycle
        updateTimes(2);

        assertTrue(game.doZombiesReachedTheHouse());
        assertTrue(game.hasGameFinished());
        assertFalse(game.playerWins());
        assertFalse(game.playerQuits());
    }

    @Test
    void aWallNut_stopsAZombieFromReachingTheHouse() {
        game.addGameObject("z", at(0, 1));
        game.addGameObjectForFree("wn", at(0, 0));
        updateTimes(6);

        assertFalse(game.doZombiesReachedTheHouse());
    }

    // ------------------------------------------------------------------ //
    //  quit / reset                                                        //
    // ------------------------------------------------------------------ //

    @Test
    void quit_setsGameFinished_andPlayerQuits() {
        game.quit();
        assertTrue(game.hasGameFinished());
        assertTrue(game.playerQuits());
    }

    @Test
    void reset_restoresCoinsCyclesAndZombies() {
        game.addGameObject("s", at(0, 0));
        updateTimes(2);

        game.reset();

        assertEquals(50, game.getCoins());
        assertEquals(0, game.getCycles());
        assertEquals(Level.EASY.getNumberOfZombies(), game.getRemainingZombies());
    }

    @Test
    void reset_clearsTheBoard() {
        game.addGameObject("s", at(0, 0));
        game.addGameObject("z", at(1, 5));
        game.generateSun();

        game.reset();

        assertTrue(game.isEmpty(at(0, 0)));
        assertTrue(game.isEmpty(at(1, 5)));
        assertEquals(0, cellsWithSuns());
    }

    @Test
    void reset_clearsSunStatistics() {
        game.addGameObject(new Sun(at(0, 0), game));
        game.catchObject(at(0, 0));
        game.generateSun();

        game.reset();

        assertEquals(0, game.getGeneratedCoins());
        assertEquals(0, game.getCaughtCoins());
    }

    @Test
    void reset_allowsCatchingAgainRightAway() {
        game.addGameObject(new Sun(at(0, 0), game));
        game.catchObject(at(0, 0));

        game.reset();
        game.addGameObject(new Sun(at(0, 0), game));

        assertTrue(game.catchObject(at(0, 0)));
    }

    @Test
    void reset_discardsPendingActions() {
        int[] executed = {0};
        game.pushAction(world -> executed[0]++);

        game.reset();
        game.update();

        assertEquals(0, executed[0]);
    }

    @Test
    void reset_clearsTheQuitFlag() {
        game.quit();
        game.reset();
        assertFalse(game.playerQuits());
        assertFalse(game.hasGameFinished());
    }

    @Test
    void reset_restartsTheRandomGenerator() {
        List<Position> first = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            first.add(game.randomPosition());
        }

        game.reset();

        for (int i = 0; i < 5; i++) {
            assertEquals(first.get(i), game.randomPosition(), "draw number " + i + " after reset");
        }
    }

    @Test
    void randomPosition_isAlwaysInsideTheBoard() {
        for (int i = 0; i < 200; i++) {
            assertTrue(game.isInsideBoard(game.randomPosition()));
        }
    }

    // ------------------------------------------------------------------ //
    //  reset(level, seed)                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void resetWithParameters_changesTheLevel() {
        game.reset(Level.HARD, 7L);
        assertEquals(Level.HARD.getNumberOfZombies(), game.getRemainingZombies());
    }

    @Test
    void resetWithParameters_restartsTheWholeGame() {
        game.addGameObject("s", at(0, 0));
        updateTimes(3);

        game.reset(Level.INSANE, 3L);

        assertEquals(0, game.getCycles());
        assertEquals(50, game.getCoins());
        assertTrue(game.isEmpty(at(0, 0)));
        assertEquals(Level.INSANE.getNumberOfZombies(), game.getRemainingZombies());
    }

    @Test
    void resetWithParameters_usesTheNewSeed() {
        Game reference = new Game(7L, Level.HARD);
        game.reset(Level.HARD, 7L);

        for (int i = 0; i < 5; i++) {
            assertEquals(reference.randomPosition(), game.randomPosition(), "draw " + i);
        }
    }

    
    @Test
    void resetWithoutParameters_afterResetWithParameters_goesBackToPreviousSettings() {
        // "recibir 0 parámetros y, por tanto, utilizar el nivel y semilla
        // proporcionados como parámetro de línea de comandos"
        game.reset(Level.HARD, 7L);

        game.reset();

        assertEquals(Level.HARD.getNumberOfZombies(), game.getRemainingZombies(),
                "level goes back to the last one configured");
    }
}
