package test;

import pvz.logic.GameWorld;
import pvz.logic.actions.GameAction;
import pvz.logic.gameobjects.GameObject;
import utils.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Minimal {@link GameWorld} that records every interaction a game object has with
 * its world. It lets unit tests exercise a single game object without the
 * randomness (zombie spawning, random suns) of the real {@code Game}.
 */
public class FakeGameWorld implements GameWorld {

    public record Attack(Position position, int damage) {}

    /** Cells reported as non-empty by {@link #isEmpty(Position)}. */
    public final Set<Position> occupied = new HashSet<>();

    public final List<Attack> zombieAttacks = new ArrayList<>();
    public final List<Attack> plantAttacks = new ArrayList<>();
    public final List<GameAction> pushedActions = new ArrayList<>();
    public final List<GameObject> addedObjects = new ArrayList<>();

    public int generateSunCalls;
    public int generatedCoins;
    public int zombiesKilled;

    /** Value returned by {@link #randomPosition()}. */
    public Position nextRandomPosition = new Position(0, 0);
    /** Result of {@link #addGameObject(GameObject)}. */
    public boolean acceptNewObjects = true;
    /** Hook invoked every time {@link #generateSun()} is called. */
    public Runnable onGenerateSun = () -> {};

    /** Executes (and forgets) the actions pushed so far, against this fake world. */
    public void runPushedActions() {
        List<GameAction> pending = new ArrayList<>(pushedActions);
        pushedActions.clear();
        for (GameAction action : pending) {
            action.execute(this);
        }
    }

    @Override
    public String positionToString(Position position) {
        return "";
    }

    @Override
    public boolean isEmpty(Position position) {
        return !occupied.contains(position);
    }

    @Override
    public boolean isInsideBoard(Position p) {
        return p.row() >= 0 && p.row() < NUM_ROWS && p.column() >= 0 && p.column() < NUM_COLS;
    }

    @Override
    public boolean isInBoardLimit(Position p) {
        return p.row() >= 0 && p.row() < NUM_ROWS && p.column() >= -1 && p.column() <= NUM_COLS;
    }

    @Override
    public Position randomPosition() {
        return nextRandomPosition;
    }

    @Override
    public void generateSun() {
        generateSunCalls++;
        onGenerateSun.run();
    }

    @Override
    public void generateCoins(int coins) {
        generatedCoins += coins;
    }

    @Override
    public boolean zombieAttack(Position p, int damage) {
        return zombieAttacks.add(new Attack(p, damage));
    }

    @Override
    public boolean plantAttack(Position p, int damage) {
        return plantAttacks.add(new Attack(p, damage));
    }

    @Override
    public boolean addGameObject(GameObject gameObject) {
        if (acceptNewObjects) {
            addedObjects.add(gameObject);
        }
        return acceptNewObjects;
    }

    @Override
    public void zombieKilled() {
        zombiesKilled++;
    }

    @Override
    public void pushAction(GameAction gameAction) {
        pushedActions.add(gameAction);
    }
}
