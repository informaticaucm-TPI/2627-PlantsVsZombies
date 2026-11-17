package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pvz.logic.gameobjects.GameObject;
import pvz.logic.gameobjects.Plant;
import pvz.logic.gameobjects.Zombie;
import pvz.logic.gameobjects.factories.PlantFactory;
import pvz.logic.gameobjects.factories.ZombieFactory;
import test.FakeGameWorld;
import utils.Position;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The factories hide how plants and zombies are created and expose the catalogue
 * used by {@code list}, {@code listZombies} and the {@code add*} commands.
 */
class FactoriesTest {

    private static final Position P = new Position(1, 2);

    private FakeGameWorld world;

    @BeforeEach
    void setUp() {
        world = new FakeGameWorld();
    }

    // ------------------------------------------------------------------ //
    //  PlantFactory                                                        //
    // ------------------------------------------------------------------ //

    @Test
    void plantCatalogue_isInTheOrderOfTheStatement() {
        List<String> descriptions = PlantFactory.getAvailablePlants().stream()
                .map(Plant::getDescription).toList();

        assertEquals(List.of(
                "[S]unflower: cost='20' suncoins, damage='0', endurance='1'",
                "[P]eashooter: cost='50' suncoins, damage='1', endurance='3'",
                "[W]all-[N]ut: cost='50' suncoins, damage='0', endurance='10'",
                "[C]herry-Bomb: cost='50' suncoins, damage='10', endurance='2'"), descriptions);
    }

    @Test
    void spawnPlant_byFullNameAndBySymbol() {
        String[][] namesAndSymbols = {
                {"sunflower", "s"}, {"peashooter", "p"}, {"wall-nut", "wn"}, {"cherry-bomb", "c"}};
        for (String[] pair : namesAndSymbols) {
            Plant byName = PlantFactory.spawnPlant(pair[0], world, P);
            Plant bySymbol = PlantFactory.spawnPlant(pair[1], world, P);
            assertNotNull(byName, pair[0]);
            assertNotNull(bySymbol, pair[1]);
            assertEquals(byName.getDescription(), bySymbol.getDescription());
        }
    }

    @Test
    void spawnPlant_isCaseInsensitive() {
        assertNotNull(PlantFactory.spawnPlant("SUNFLOWER", world, P));
        assertNotNull(PlantFactory.spawnPlant("Wall-Nut", world, P));
        assertNotNull(PlantFactory.spawnPlant("WN", world, P));
        assertNotNull(PlantFactory.spawnPlant("C", world, P));
    }

    @Test
    void spawnPlant_unknownName_returnsNull() {
        assertNull(PlantFactory.spawnPlant("tree", world, P));
        assertNull(PlantFactory.spawnPlant("", world, P));
        assertNull(PlantFactory.spawnPlant("peashoot", world, P));
        assertNull(PlantFactory.spawnPlant("zombie", world, P));
    }

    @Test
    void spawnPlant_createsANewPlantAtThePosition_eachTime() {
        Plant first = PlantFactory.spawnPlant("s", world, P);
        Plant second = PlantFactory.spawnPlant("s", world, P);

        assertNotSame(first, second);
        assertTrue(first.isInPosition(P));
        assertTrue(first.isAlive());
    }

    @Test
    void plantCatalogue_cannotBeModifiedByClients() {
        List<Plant> catalogue = PlantFactory.getAvailablePlants();
        Plant extra = PlantFactory.spawnPlant("s", world, P);
        assertThrows(UnsupportedOperationException.class, () -> catalogue.add(extra));
        assertThrows(UnsupportedOperationException.class, catalogue::clear);
    }

    // ------------------------------------------------------------------ //
    //  ZombieFactory                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void zombieCatalogue_hasTheFourKindsOfZombie() {
        Set<String> descriptions = ZombieFactory.getAvailableZombies().stream()
                .map(Zombie::getDescription).collect(Collectors.toSet());

        assertEquals(Set.of(
                "Zombie: speed='2', damage='1', endurance='5'",
                "BucketHead Zombie: speed='4', damage='1', endurance='8'",
                "Sporty Zombie: speed='1', damage='1', endurance='2'",
                "Explosive Zombie: speed='2', damage='1', endurance='5'"), descriptions);
    }

    @Test
    void spawnZombie_bySymbol() {
        String[][] symbolsAndIcons = {
                {"z", " Z[05]"}, {"bz", "Bz[08]"}, {"sz", "Sz[02]"}, {"ez", "Ez[05]"}};
        for (String[] pair : symbolsAndIcons) {
            Zombie zombie = ZombieFactory.spawnZombie(pair[0], world, P);
            assertNotNull(zombie, pair[0]);
            assertEquals(pair[1], zombie.getIcon());
        }
    }

    @Test
    void spawnZombie_byFullName_isCaseInsensitive() {
        assertNotNull(ZombieFactory.spawnZombie("zombie", world, P));
        assertNotNull(ZombieFactory.spawnZombie("BUCKETHEAD ZOMBIE", world, P));
        assertNotNull(ZombieFactory.spawnZombie("Sporty Zombie", world, P));
        assertNotNull(ZombieFactory.spawnZombie("explosive zombie", world, P));
        assertNotNull(ZombieFactory.spawnZombie("BZ", world, P));
    }

    @Test
    void spawnZombie_unknownName_returnsNull() {
        assertNull(ZombieFactory.spawnZombie("ghost", world, P));
        assertNull(ZombieFactory.spawnZombie("", world, P));
        assertNull(ZombieFactory.spawnZombie("sunflower", world, P));
    }

    @Test
    void spawnZombie_createsANewZombieAtThePosition_eachTime() {
        Zombie first = ZombieFactory.spawnZombie("z", world, P);
        Zombie second = ZombieFactory.spawnZombie("z", world, P);

        assertNotSame(first, second);
        assertTrue(first.isInPosition(P));
        assertTrue(first.isAlive());
    }

    @Test
    void zombieCatalogue_cannotBeModifiedByClients() {
        List<Zombie> catalogue = ZombieFactory.getAvailableZombies();
        Zombie extra = ZombieFactory.spawnZombie("z", world, P);
        assertThrows(UnsupportedOperationException.class, () -> catalogue.add(extra));
        assertThrows(UnsupportedOperationException.class, catalogue::clear);
    }

    @Test
    void plantsAndZombies_doNotShareNames() {
        for (Plant plant : PlantFactory.getAvailablePlants()) {
            GameObject asZombie = ZombieFactory.spawnZombie(plant.getSymbol(), world, P);
            assertNull(asZombie, plant.getSymbol() + " is a plant symbol");
        }
        for (Zombie zombie : ZombieFactory.getAvailableZombies()) {
            GameObject asPlant = PlantFactory.spawnPlant(zombie.getSymbol(), world, P);
            assertNull(asPlant, zombie.getSymbol() + " is a zombie symbol");
        }
    }
}
