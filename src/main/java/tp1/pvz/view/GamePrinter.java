package tp1.pvz.view;

import static tp1.pvz.view.Messages.PROMPT;
import static tp1.pvz.view.Messages.debug;
import static tp1.pvz.view.Messages.error;

import tp1.pvz.logic.Game;
import tp1.pvz.utils.Position;
import tp1.pvz.utils.StringUtils;

import java.util.Scanner;

/**
 * View component of the game (the V in MVC).
 *
 * <p>Renders the current game state as a formatted string: a status header
 * (cycle count, sun coins, remaining zombies) followed by the ASCII board grid.
 * Also produces the end-of-game message distinguishing player win, player quit,
 * and zombie win outcomes.
 *
 * <p>This class is read-only with respect to {@link Game}: it queries game state
 * through the public API and never modifies it. All string literals and format
 * templates are sourced from {@link Messages}.
 */
public class GamePrinter implements GameView {

	private static final String SPACE = " ";

	private static final String CELL_BORDER_CHAR = "─";

	private static final String VERTICAL_DELIMITER = "|";

	private static final String NEW_LINE = System.lineSeparator();

	private static final int MARGIN_SIZE = 2;

	private static final String MARGIN = repeat(SPACE, MARGIN_SIZE);

	private static final int CELL_SIZE = 12;

	private static final String CELL_BORDER = repeat(CELL_BORDER_CHAR, CELL_SIZE);

	private static final String ROW_BORDER = SPACE + repeat(CELL_BORDER + SPACE, Game.NUM_COLS);

	private static final String INDENTED_ROW_BORDER = String.format("%n%s%s%n", MARGIN, ROW_BORDER);

	private GameStatus game;
	private Scanner scanner;

	public GamePrinter(GameStatus game) {
		this.game = game;
		this.scanner = new Scanner(System.in);
	}

	/**
	 * Builds a string that represent the game status: cycles, suncoins, remaining zombies.
	 *
	 * @return the string that represents the game status.
	 */
	private String getInfo() {
		StringBuilder buffer = new StringBuilder();

		// TODO fill your code

		return buffer.toString();
	}

	/**
	 * Prevents to print several suns in the same cell
	 *
	 * @param repr The string representation of the objects of the cell,
	 *             which may contain several suns. Only the first one will be kept
	 */
	private String filterRepeatedSuns(String repr) {

		// Build the pattern that represents a Sun
		Pattern sunRepresentationPattern = Pattern.compile(Messages.GAME_OBJECT_ICON
				.formatted("\\" + SUN_SYMBOL, 0)
				.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("00", "\\d+")
		);

		Matcher matcher = sunRepresentationPattern.matcher(repr);

		if (matcher.find()) {
			StringBuilder sb = new StringBuilder();

			// 1. Add everything up to the end of the first match (includes the match)
			sb.append(repr, 0, matcher.end());

			int lastEnd = matcher.end();
			// 2. Loop through subsequent matches and keep ONLY the non-matching text between them
			while (matcher.find()) {
				sb.append(repr, lastEnd, matcher.start());
				lastEnd = matcher.end();
			}
			// 3. Append any remaining non-matching text at the end of the string
			sb.append(repr.substring(lastEnd));

			return sb.toString();
		}

		// If no match was found, return the original text unchanged
		return repr;
	}

	/**
	 * Prints complete representation (status+board) of the game.
	 */
	@Override
	public void showGame() {
		StringBuilder str = new StringBuilder();

		// Game Status
		str.append(getInfo());

		// Paint game board
		str.append(INDENTED_ROW_BORDER);

		for (int row = 0; row < GameWorld.NUM_ROWS; row++) {
			str.append(MARGIN).append(VERTICAL_DELIMITER);
			for (int col = 0; col < GameWorld.NUM_COLS; col++) {
				String objetsInPosition = filterRepeatedSuns(game.positionToString(new Position(row, col)));
				str.append(StringUtils.centre(objetsInPosition, CELL_SIZE)).append(VERTICAL_DELIMITER);
			}
			str.append(INDENTED_ROW_BORDER);
		}

		System.out.println(str);
	}

	/**
	 * Prints the message to be printed once the game has finished.
	 */
	@Override
	public void showEndMessage() {
		StringBuilder buffer = new StringBuilder(Messages.GAME_OVER);
		// TODO fill your code
		System.out.println(buffer);
	}

	@Override
	public void showError(String message) {
		System.out.println(error(message));
	}

	@Override
	public void showMessage(String message) {
		System.out.println(message);
	}

	@Override
	public String[] getPrompt() {
		System.out.print(PROMPT);
		String line = scanner.nextLine();
		String[] words = line.toLowerCase().trim().split("\\s+");

		System.out.println(debug(line));

		return words;
	}
}
