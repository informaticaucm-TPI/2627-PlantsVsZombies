package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Complete tests for the record feature. Unlike {@link P3CompleteTest}, each test prepares its own
 * {@code record.txt} (the game always reads it from the working directory), so they do not depend
 * on the order of execution. The file that was there before is restored afterwards.
 *
 * <p>Tests 03, 04 and 06 reuse the input of p3 test 07, which finishes a HARD game with 50 points.
 */
public class P3RecordsCompleteTest extends CompleteTest {

  private static final Path RECORD_FILE = Path.of("record.txt");

  private byte[] original;

  public P3RecordsCompleteTest() {
    super("test/resources/p3-records");
  }

  @BeforeEach
  void backUpRecordFile() throws IOException {
    original = Files.exists(RECORD_FILE) ? Files.readAllBytes(RECORD_FILE) : null;
    Files.deleteIfExists(RECORD_FILE);
  }

  @AfterEach
  void restoreRecordFile() throws IOException {
    RECORD_FILE.toFile().setWritable(true);
    if (original != null)
      Files.write(RECORD_FILE, original);
    else
      Files.deleteIfExists(RECORD_FILE);
  }

  private static void writeRecords(String... lines) throws IOException {
    Files.write(RECORD_FILE, List.of(lines));
  }

  private static List<String> readRecords() throws IOException {
    return Files.readAllLines(RECORD_FILE);
  }

  /** The record of the current level is shown, follows resets, and does not consume cycles. */
  @Test
  public void showsTheRecordOfTheCurrentLevel() throws IOException {
    writeRecords("HARD:30", "EASY:10", "INSANE:0");
    testN(0);
    // nobody beat a record, so the file is left untouched
    assertEquals(List.of("HARD:30", "EASY:10", "INSANE:0"), readRecords());
  }

  /** There is no record file: the game reports it and finishes without starting. */
  @Test
  public void missingRecordFile_finishesTheGame() {
    testN(1);
  }

  /** The record file has a negative value: the game reports it and finishes without starting. */
  @Test
  public void corruptedRecordFile_finishesTheGame() throws IOException {
    writeRecords("EASY:10", "HARD:-5");
    testN(2);
  }

  /** The record is beaten but the file cannot be written: the error is shown and the game ends. */
  @Test
  public void unwritableRecordFile_showsTheError() throws IOException {
    writeRecords("HARD:0", "EASY:0", "INSANE:0");
    RECORD_FILE.toFile().setWritable(false);
    // running as root ignores the permissions
    assumeFalse(Files.isWritable(RECORD_FILE), "the record file is still writable");
    testN(3);
  }

  /** A beaten record is saved and shown in the next game. */
  @Test
  public void beatenRecord_isSavedAndShownInTheNextGame() throws IOException {
    writeRecords("HARD:20", "EASY:5", "INSANE:0");
    testN(4);
    assertEquals(List.of("EASY:5", "HARD:50", "INSANE:0"), readRecords().stream().sorted().toList());
    testN(5);
  }

  /** A score that does not beat the record leaves the file untouched. */
  @Test
  public void notBeatenRecord_isKept() throws IOException {
    writeRecords("HARD:100", "EASY:0", "INSANE:0");
    testN(6);
    assertEquals(List.of("HARD:100", "EASY:0", "INSANE:0"), readRecords());
  }
}
