package test;

import org.junit.jupiter.api.Test;
import pvz.control.Level;
import pvz.logic.Game;
import pvz.logic.GameWorld;
import pvz.logic.gameobjects.Peashooter;
import pvz.logic.gameobjects.Plant;
import pvz.logic.gameobjects.Sun;
import test.FakeGameWorld.Attack;
import utils.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Peashooter: cost 50, damage 1, endurance 3. Every cycle it asks the world to
 * attack from its own cell towards the right; it never touches a zombie directly.
 */
class PeashooterTest extends AbstractPlantTest {

    @Override
    protected Plant create(Position position, GameWorld world) {
        return new Peashooter(position, world);
    }

    @Override
    protected String expectedListName() {
        return "[P]eashooter";
    }

    @Override
    protected String expectedSymbol() {
        return "P";
    }

    @Override
    protected int expectedCost() {
        return 50;
    }

    @Override
    protected int expectedDamage() {
        return 1;
    }

    @Override
    protected int expectedEndurance() {
        return 3;
    }

    @Test
    void cost_constantIs50() {
        assertEquals(50, Peashooter.COST);
    }

    // ------------------------------------------------------------------ //
    //  Shooting                                                            //
    // ------------------------------------------------------------------ //

    @Test
    void update_shootsFromItsOwnCell_withDamage1() {
        plant.update();
        assertEquals(1, world.plantAttacks.size());
        assertEquals(new Attack(POS, 1), world.plantAttacks.get(0));
    }

    @Test
    void update_shootsEveryCycle() {
        for (int i = 0; i < 5; i++) {
            plant.update();
        }
        assertEquals(5, world.plantAttacks.size());
    }

    @Test
    void update_neverHurtsPlants() {
        plant.update();
        assertTrue(world.zombieAttacks.isEmpty());
    }

    @Test
    void update_doesNotGenerateSunsOrCoins() {
        plant.update();
        assertEquals(0, world.generateSunCalls);
        assertEquals(0, world.generatedCoins);
    }

    @Test
    void deadPeashooter_doesNotShoot() {
        plant.receiveZombieAttack(99);
        plant.update();
        assertTrue(world.plantAttacks.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Shooting with the real game                                         //
    // ------------------------------------------------------------------ //

    private static String cell(Game game, int row, int col) {
        return game.positionToString(new Position(row, col));
    }

    @Test
    void shot_hitsTheFirstZombieOfItsRow() {
        Game game = new Game(0L, Level.EASY);
        Peashooter shooter = new Peashooter(new Position(0, 1), game);
        game.addGameObject("z", new Position(0, 4));
        game.addGameObject("z", new Position(0, 6));

        shooter.update();

        assertEquals(" Z[04]", cell(game, 0, 4), "nearest zombie takes the pea");
        assertEquals(" Z[05]", cell(game, 0, 6), "the pea does not go through the first zombie");
    }

    @Test
    void shot_ignoresZombiesOfOtherRows() {
        Game game = new Game(0L, Level.EASY);
        Peashooter shooter = new Peashooter(new Position(0, 1), game);
        game.addGameObject("z", new Position(1, 4));

        shooter.update();

        assertEquals(" Z[05]", cell(game, 1, 4));
    }

    @Test
    void shot_ignoresZombiesBehindThePlant() {
        Game game = new Game(0L, Level.EASY);
        Peashooter shooter = new Peashooter(new Position(0, 4), game);
        game.addGameObject("z", new Position(0, 2));

        shooter.update();

        assertEquals(" Z[05]", cell(game, 0, 2));
    }

    @Test
    void shot_flyesOverSunsAndPlants() {
        Game game = new Game(0L, Level.EASY);
        Peashooter shooter = new Peashooter(new Position(0, 0), game);
        game.addGameObject("wn", new Position(0, 1));
        game.addGameObject(new Sun(new Position(0, 2), game));
        game.addGameObject("z", new Position(0, 5));

        shooter.update();

        assertEquals(" Z[04]", cell(game, 0, 5));
        assertTrue(cell(game, 0, 1).contains("WN[10]"), "the wall-nut is not hurt by the pea");
    }

    @Test
    void shot_killsASportyZombieInTwoCycles() {
        Game game = new Game(0L, Level.EASY);
        Peashooter shooter = new Peashooter(new Position(0, 0), game);
        game.addGameObject("sz", new Position(0, 5)); // endurance 2

        shooter.update();
        shooter.update();
        game.update(); // removes dead objects

        assertEquals("", cell(game, 0, 5));
    }
}
