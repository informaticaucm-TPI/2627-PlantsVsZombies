package tp1.pvz.view;

import tp1.pvz.control.Level;

/**
 * Central repository of all user-facing string literals and format templates.
 *
 * <p>Keeps UI text out of logic and control classes. Every string that appears
 * on the console — prompts, error messages, plant descriptions, icons, and
 * end-of-game banners — is defined here as a {@code public static final}
 * constant. Format strings follow {@link String#format} conventions.
 *
 * <p>This class is not instantiable; it is a pure namespace for constants and
 * two formatting helpers ({@link #debug} and {@link #error}).
 */
public class Messages {

	public static final String VERSION = "1.0";

	public static final String GAME_NAME = "PlantsVsZombies";
	
	public static final String USAGE = "Usage: %s <level> [<seed>]".formatted(GAME_NAME);
	
	public static final String USAGE_LEVEL_PARAM = "\t<level>: %s".formatted(Level.all(", "));
	
	public static final String USAGE_SEED_PARAM = "\t<seed>: %s".formatted(Messages.SEED_NOT_A_NUMBER);

	public static final String WELCOME = String.format("%s %s%n", GAME_NAME, VERSION);

	public static final String ALLOWED_LEVELS = "Level must be one of: %s".formatted(Level.all(", "));

	public static final String SEED_NOT_A_NUMBER = "The seed must be a number";

	public static final String SEED_NOT_A_NUMBER_ERROR = String.format("%s: %%s", SEED_NOT_A_NUMBER);

	public static final String CONFIGURED_LEVEL = "Level: %s";

	public static final String CONFIGURED_SEED = "Random generator initialized with seed: %d";

	public static final String PROMPT = "Command > ";

	public static final String DEBUG = "%n[DEBUG] Executing: %s%n";

	public static final String ERROR = "[ERROR] Error: %s%n";

	public static final String HELP_AVAILABLE_COMMANDS = "Available commands:";

	public static final String LINE_SEPARATOR = System.lineSeparator();

	public static final String HELP_DETAILS_COMMAND_HELP_SEPARATOR = ": ";

	public static final String UNKNOWN_COMMAND = "Unknown command";
	
	public static final String COMMAND_PARAMETERS_MISSING = "Missing parameters";

	public static final String COMMAND_INCORRECT_PARAMETER_NUMBER = "Incorrect parameter number";

	public static final String INVALID_POSITION = "Invalid position";

	public static final String INVALID_GAME_OBJECT = String.format("Invalid object%n");

	public static final String INVALID_COMMAND = "Invalid command";

	// Game objects
	public static final String GAME_OBJECT_ICON = "%2s[%02d]";
	public static final String PLANT_DESCRIPTION = "%s: cost='%d' suncoins, damage='%d', endurance='%d'";

	public static final String PEASHOOTER_SYMBOL = "P";
	public static final String PEASHOOTER_NAME = "Peashooter";
	public static final String PEASHOOTER_DESCRIPTION = "[P]eashooter: cost='%d' suncoins, damage='%d', endurance='%d'";
	public static final String PEASHOOTER_ICON = " P[%02d]";
	public static final String PEASHOOTER_NAME_AND_SHORTCUT = "[P]eashooter";

	public static final String SUNFLOWER_SYMBOL = "S";
	public static final String SUNFLOWER_NAME = "Sunflower";
	public static final String SUNFLOWER_DESCRIPTION = "[S]unflower: cost='%d' suncoins, damage='%d', endurance='%d'";
	public static final String SUNFLOWER_ICON = " S[%02d]";
	public static final String SUNFLOWER_NAME_AND_SHORTCUT = "[S]unflower";

	public static final String ZOMBIE_SYMBOL = "Z";
	public static final String ZOMBIE_NAME = "Zombie";
	public static final String ZOMBIE_DESCRIPTION = "%s: speed='%d', damage='%d', endurance='%d'";
	public static final String ZOMBIE_ICON = " Z[%02d]";
	public static final String ZOMBIE_NAME_AND_SHORTCUT = "[Z]ombie";

	public static final String WALL_NUT_SYMBOL = "WN";
	public static final String WALL_NUT_NAME = "Wall-Nut";
	public static final String WALL_NUT_NAME_SHORTCUT = "[W]all-[N]ut";

	public static final String CHERRY_BOMB_SYMBOL = "c";
	public static final String CHERRY_BOMB_NAME = "Cherry-Bomb";
	public static final String CHERRY_BOMB_NAME_SHORTCUT = "[C]herry-Bomb";

	public static final String BUCKET_HEAD_ZOMBIE_SYMBOL = "Bz";
	public static final String BUCKET_HEAD_ZOMBIE_NAME = "BucketHead Zombie";

	public static final String SPORTY_ZOMBIE_SYMBOL = "Sz";
	public static final String SPORTY_ZOMBIE_NAME = "Sporty Zombie";

	public static final String EXPLOSIVE_ZOMBIE_SYMBOL = "Ez";
	public static final String EXPLOSIVE_ZOMBIE_NAME = "Explosive Zombie";

	public static final String SUN_SYMBOL = "*";
	public static final String SUN_DESCRIPTION = "Sun";

	public static final String NOT_ENOUGH_COINS = "Not enough suncoins";

	public static final String NUMBER_OF_CYCLES = "Number of cycles: %d%n";
	
	public static final String NUMBER_OF_COINS = "Sun coins: %d%n";

	public static final String GENERATED_SUNS = "Generated suns: %d%n";

	public static final String CAUGHT_SUNS = "Caught suns: %d%n";

	public static final String REMAINING_ZOMBIES = "Remaining zombies: %d%n";
	
	public static final String GAME_OVER = "Game over";

	public static final String PLAYER_QUITS = "Player leaves the game";
	
	public static final String ZOMBIES_WIN = "Zombies win!";

	public static final String PLAYER_WINS = "Player wins!";

	public static final String EMPTY_STRING = "";

	// Commands available help and shortcuts
	public static final String COMMAND_ADD_PLANT_NAME = "add";
	public static final String COMMAND_ADD_PLANT_SHORTCUT = "a";
	public static final String COMMAND_ADD_PLANT_DETAILS = "[a]dd <plant> <col> <row>";
	public static final String COMMAND_ADD_PLANT_HELP = "add a plant in position (col, row)";

	public static final String COMMAND_ADD_ZOMBIE_NAME = "addZombie";
	public static final String COMMAND_ADD_ZOMBIE_SHORTCUT = "az";
	public static final String COMMAND_ADD_ZOMBIE_DETAILS = "[a]dd[Z]ombie <zombie> <col> <row>";
	public static final String COMMAND_ADD_ZOMBIE_HELP = "add a zombie in position (col, row)";

	public static final String COMMAND_LIST_PLANTS_NAME = "list";
	public static final String COMMAND_LIST_PLANTS_SHORTCUT = "l";
	public static final String COMMAND_LIST_PLANTS_DETAILS = "[l]ist";
	public static final String COMMAND_LIST_PLANTS_HELP = "print the list of available plants";

	public static final String COMMAND_LIST_ZOMBIES_NAME = "listZombies";
	public static final String COMMAND_LIST_ZOMBIES_SHORTCUT = "lz";
	public static final String COMMAND_LIST_ZOMBIES_DETAILS = "[l]ist[Z]ombies";
	public static final String COMMAND_LIST_ZOMBIES_HELP = "print the list of available zombies";

	public static final String COMMAND_RESET_NAME = "reset";
	public static final String COMMAND_RESET_SHORTCUT = "r";
	public static final String COMMAND_RESET_DETAILS = "[r]eset";
	// TODO: use me in part 2 (and remove the old command details)
	// public static final String COMMAND_RESET_DETAILS = "[r]eset [<level> <seed>]";

	public static final String COMMAND_RESET_HELP = "start a new game";
	// TODO: use me in part 2 (and remove the old command help)
	// public static final String COMMAND_RESET_HELP = "start a new game (if level and seed are both provided, they are used to initialize the game)";

	public static final String COMMAND_HELP_NAME = "help";
	public static final String COMMAND_HELP_SHORTCUT = "h";
	public static final String COMMAND_HELP_DETAILS = "[h]elp";
	public static final String COMMAND_HELP_HELP = "print this help message";

	public static final String COMMAND_EXIT_NAME = "exit";
	public static final String COMMAND_EXIT_SHORTCUT = "e";
	public static final String COMMAND_EXIT_DETAILS = "[e]xit";
	public static final String COMMAND_EXIT_HELP = "terminate the program";

	public static final String COMMAND_UPDATE_NAME = "none";
	public static final String COMMAND_UPDATE_SHORTCUT = "n";
	public static final String COMMAND_UPDATE_DETAILS = "[n]one | \"\"";
	public static final String COMMAND_UPDATE_HELP = "skips cycle";

	public static final String COMMAND_CHEAT_PLANT_NAME = "cheatPlant";
	public static final String COMMAND_CHEAT_PLANT_SHORTCUT = "cp";
	public static final String COMMAND_CHEAT_PLANT_DETAILS = "[C]heat[P]lant <plant> <col> <row>";
	public static final String COMMAND_CHEAT_PLANT_HELP = "add a plant in position (col, row) without consuming suncoins";

	public static final String COMMAND_CATCH_NAME = "catch";
	public static final String COMMAND_CATCH_SHORTCUT = "c";
	public static final String COMMAND_CATCH_DETAILS = "[C]atch <col> <row>";
	public static final String COMMAND_CATCH_HELP = "catch a sun, if possible, in position (col, row)";

	public static final String NO_CATCHABLE_IN_POSITION = "No sun in (%d, %d)";

	/**
	 * Formats a debug message.
	 * 
	 * @param message debug message
	 * 
	 * @return the formated debug message;
	 */
	public static final String debug(String message) {
		return Messages.DEBUG.formatted(message);
	}

	/**
	 * Formats an error message.
	 * 
	 * @param message Error message
	 * 
	 * @return the formated error message;
	 */
	public static final String error(String message) {
		return Messages.ERROR.formatted(message);
	}
}
