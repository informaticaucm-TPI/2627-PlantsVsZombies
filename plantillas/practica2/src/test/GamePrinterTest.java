package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import pvz.logic.GameModel;
import pvz.logic.GameStatus;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.SportyZombie;
import pvz.logic.gameobjects.Sun;
import pvz.logic.gameobjects.Sunflower;
import pvz.view.GamePrinter;
import utils.Position;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * How the console view draws the game: the status header (including the sun
 * statistics of part II) and the board, where a cell can show one plant or zombie
 * plus suns.
 */
class GamePrinterTest {

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream buffer;

    private GameWorld gw;
    private GameModel gm;
    private GamePrinter printer;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalIn = System.in;
        buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        Game game = new Game(0L, Level.EASY);
        gw = game;
        gm = game;
        printer = new GamePrinter(game);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private String show() {
        buffer.reset();
        printer.showGame();
        return buffer.toString();
    }

    /** The four board rows, split into the 8 board cells plus the cell outside the board. */
    private List<String[]> rows(String output) {
        return output.lines()
                .filter(line -> line.startsWith("  |"))
                .map(line -> line.split("\\|", -1))
                .toList();
    }

    /** Content of the board cell (row, col). Parts: [margin, cell0..cell7, "", " outside"]. */
    private String cell(String output, int row, int col) {
        return rows(output).get(row)[1 + col];
    }

    private String outsideCell(String output, int row) {
        String[] parts = rows(output).get(row);
        return parts[parts.length - 1].trim();
    }

    // ------------------------------------------------------------------ //
    //  Header                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void header_showsCyclesCoinsZombiesAndSunStatistics_inThatOrder() {
        List<String> lines = show().lines().toList();
        assertEquals("Number of cycles: 0", lines.get(0));
        assertEquals("Sun coins: 50", lines.get(1));
        assertEquals("Remaining zombies: 3", lines.get(2));
        assertEquals("Generated suns: 0", lines.get(3));
        assertEquals("Caught suns: 0", lines.get(4));
    }

    @Test
    void header_reflectsGeneratedAndCaughtSuns() {
        gw.addGameObject(new Sun(new Position(0, 0), gw));
        gm.catchObject(new Position(0, 0));
        gw.generateSun();

        String out = show();

        assertTrue(out.contains("Sun coins: 60"));
        assertTrue(out.contains("Generated suns: 1"));
        assertTrue(out.contains("Caught suns: 1"));
    }

    // ------------------------------------------------------------------ //
    //  Board                                                               //
    // ------------------------------------------------------------------ //

    @Test
    void newPlantsAndZombies_areDrawnWithTheirSymbols() {
        gm.addGameObjectForFree("wn", new Position(0, 0));
        gm.addGameObjectForFree("c", new Position(1, 1));
        gm.addGameObject("bz", new Position(2, 2));
        gm.addGameObject("sz", new Position(3, 3));
        gm.addGameObject("ez", new Position(0, 4));

        String out = show();

        assertEquals("WN[10]", cell(out, 0, 0).trim());
        assertEquals("c[02]", cell(out, 1, 1).trim());
        assertEquals("Bz[08]", cell(out, 2, 2).trim());
        assertEquals("Sz[02]", cell(out, 3, 3).trim());
        assertEquals("Ez[05]", cell(out, 0, 4).trim());
    }

    @Test
    void aSun_isDrawnWithItsRemainingCycles() {
        gw.addGameObject(new Sun(new Position(2, 5), gw));
        assertEquals("*[11]", cell(show(), 2, 5).trim());
    }

    @Test
    void aSunAndAPlant_shareACell() {
        gw.addGameObject(new Sunflower(new Position(0, 0), gw));
        gw.addGameObject(new Sun(new Position(0, 0), gw));
        assertEquals(" S[01] *[11]", cell(show(), 0, 0));
    }

    @Test
    void aSunAndAZombie_shareACell() {
        gw.addGameObject(new Sun(new Position(0, 5), gw));
        gw.addGameObject(new SportyZombie(new Position(0, 5), gw));
        String cell = cell(show(), 0, 5);
        assertTrue(cell.contains("*[11]"));
        assertTrue(cell.contains("Sz[02]"));
    }
}
