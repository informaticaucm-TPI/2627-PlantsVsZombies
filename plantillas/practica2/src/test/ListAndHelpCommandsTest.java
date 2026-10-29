package test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The informative commands: {@code list}, {@code listZombies} and {@code help}. None
 * of them changes the game or redraws the board; their output is the one printed in
 * the statement.
 */
class ListAndHelpCommandsTest extends CommandTestBase {

    private void assertGameUntouched() {
        assertEquals(0, game.getCycles());
        assertEquals(50, game.getCoins());
        assertEquals(0, view.gamesShown);
        assertTrue(view.errors.isEmpty());
        assertEquals(1, view.messages.size());
    }

    // ------------------------------------------------------------------ //
    //  list                                                                //
    // ------------------------------------------------------------------ //

    @Test
    void list_printsTheFourPlants_inTheOrderOfTheStatement() {
        run("list");

        assertEquals(List.of(
                "[S]unflower: cost='20' suncoins, damage='0', endurance='1'",
                "[P]eashooter: cost='50' suncoins, damage='1', endurance='3'",
                "[W]all-[N]ut: cost='50' suncoins, damage='0', endurance='10'",
                "[C]herry-Bomb: cost='50' suncoins, damage='10', endurance='2'"),
                view.lastMessageLines());
    }

    @Test
    void list_shortcut_printsTheSame() {
        run("list");
        List<String> byName = view.lastMessageLines();
        view.messages.clear();
        run("l");
        assertEquals(byName, view.lastMessageLines());
    }

    @Test
    void list_doesNotChangeTheGame() {
        run("list");
        assertGameUntouched();
    }

    // ------------------------------------------------------------------ //
    //  listZombies                                                         //
    // ------------------------------------------------------------------ //

    private List<String> zombieLines() {
        return view.lastMessageLines().stream().filter(line -> line.contains("speed=")).toList();
    }

    @Test
    void listZombies_printsTheFourZombies() {
        run("listZombies");

        assertEquals(4, zombieLines().size());
        assertTrue(zombieLines().containsAll(List.of(
                "Zombie: speed='2', damage='1', endurance='5'",
                "BucketHead Zombie: speed='4', damage='1', endurance='8'",
                "Sporty Zombie: speed='1', damage='1', endurance='2'",
                "Explosive Zombie: speed='2', damage='1', endurance='5'")));
    }

    
    @Test
    void listZombies_isInTheOrderOfTheStatement() {
        run("lz");
        assertEquals(List.of(
                "Zombie: speed='2', damage='1', endurance='5'",
                "BucketHead Zombie: speed='4', damage='1', endurance='8'",
                "Sporty Zombie: speed='1', damage='1', endurance='2'",
                "Explosive Zombie: speed='2', damage='1', endurance='5'"),
                zombieLines());
    }

    @Test
    void listZombies_doesNotChangeTheGame() {
        run("lz");
        assertGameUntouched();
    }

    // ------------------------------------------------------------------ //
    //  help                                                                //
    // ------------------------------------------------------------------ //

    @Test
    void help_printsEveryCommand_asInTheStatement() {
        run("help");

        assertEquals(List.of(
                "Available commands:",
                "[a]dd <plant> <col> <row>: add a plant in position (col, row)",
                "[a]dd[Z]ombie <zombie> <col> <row>: add a zombie in position (col, row)",
                "[C]heat[P]lant <plant> <col> <row>: add a plant in position (col, row) without consuming suncoins",
                "[C]atch <col> <row>: catch a sun, if possible, in position (col, row)",
                "[l]ist: print the list of available plants",
                "[l]ist[Z]ombies: print the list of available zombies",
                "[r]eset [<level> <seed>]: start a new game (if level and seed are both provided, they are used to initialize the game)",
                "[h]elp: print this help message",
                "[e]xit: terminate the program",
                "[n]one | \"\": skips cycle"),
                view.lastMessageLines());
    }

    @Test
    void help_shortcut_printsTheSame() {
        run("help");
        List<String> byName = view.lastMessageLines();
        view.messages.clear();
        run("h");
        assertEquals(byName, view.lastMessageLines());
    }

    @Test
    void help_doesNotChangeTheGame() {
        run("h");
        assertGameUntouched();
    }
}
