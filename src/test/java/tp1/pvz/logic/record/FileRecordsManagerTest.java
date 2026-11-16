package tp1.pvz.logic.record;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tp1.pvz.control.Level;
import tp1.pvz.control.exception.RecordException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileRecordsManagerTest {

  @TempDir
  Path dir;

  private File file;

  @BeforeEach
  void setUp() {
    file = dir.resolve("record.txt").toFile();
  }

  private void write(String... lines) throws IOException {
    Files.write(file.toPath(), List.of(lines));
  }

  private FileRecordsManager loaded(String... lines) throws IOException, RecordException {
    write(lines);
    FileRecordsManager manager = new FileRecordsManager(file);
    manager.load();
    return manager;
  }

  // ---- load: valid files ----

  @Test
  void load_readsEveryLevel() throws Exception {
    FileRecordsManager m = loaded("HARD:20", "EASY:30", "INSANE:40");
    assertEquals(30, m.getRecord(Level.EASY));
    assertEquals(20, m.getRecord(Level.HARD));
    assertEquals(40, m.getRecord(Level.INSANE));
  }

  @Test
  void load_orderOfLinesDoesNotMatter() throws Exception {
    FileRecordsManager m = loaded("INSANE:3", "EASY:1", "HARD:2");
    assertEquals(1, m.getRecord(Level.EASY));
    assertEquals(2, m.getRecord(Level.HARD));
    assertEquals(3, m.getRecord(Level.INSANE));
  }

  @Test
  void load_missingLevel_defaultsToZero() throws Exception {
    FileRecordsManager m = loaded("EASY:25");
    assertEquals(25, m.getRecord(Level.EASY));
    assertEquals(0, m.getRecord(Level.HARD));
    assertEquals(0, m.getRecord(Level.INSANE));
  }

  @Test
  void load_emptyFile_allRecordsAreZero() throws Exception {
    FileRecordsManager m = loaded();
    for (Level level : Level.values())
      assertEquals(0, m.getRecord(level));
  }

  @Test
  void load_ignoresBlankLinesAndIsCaseInsensitive() throws Exception {
    FileRecordsManager m = loaded("", "easy:5", "   ", "Hard : 7");
    assertEquals(5, m.getRecord(Level.EASY));
    assertEquals(7, m.getRecord(Level.HARD));
  }

  @Test
  void load_zeroIsAValidRecord() throws Exception {
    FileRecordsManager m = loaded("EASY:0", "HARD:-0");
    assertEquals(0, m.getRecord(Level.EASY));
    assertEquals(0, m.getRecord(Level.HARD));
  }

  // ---- load: corrupted or missing files ----

  @Test
  void load_fileDoesNotExist_throwsRecordException() {
    FileRecordsManager m = new FileRecordsManager(file);
    RecordException e = assertThrows(RecordException.class, m::load);
    assertTrue(e.getMessage().contains(file.toString()));
  }

  @Test
  void load_unknownLevel_throwsRecordException() throws Exception {
    write("EXTREME:10");
    assertThrows(RecordException.class, new FileRecordsManager(file)::load);
  }

  @Test
  void load_valueNotANumber_throwsRecordException() throws Exception {
    write("EASY:abc");
    RecordException e = assertThrows(RecordException.class, new FileRecordsManager(file)::load);
    assertTrue(e.getMessage().contains("EASY:abc"), "the message should point at the bad line");
  }

  @Test
  void load_negativeValue_throwsRecordException() throws Exception {
    write("EASY:-5");
    RecordException e = assertThrows(RecordException.class, new FileRecordsManager(file)::load);
    assertTrue(e.getMessage().contains("EASY:-5"), "the message should point at the bad line");
  }

  @Test
  void load_negativeValueAmongValidOnes_throwsRecordException() throws Exception {
    write("HARD:20", "EASY:-1", "INSANE:40");
    assertThrows(RecordException.class, new FileRecordsManager(file)::load);
  }

  @Test
  void load_missingSeparator_throwsRecordException() throws Exception {
    write("EASY 10");
    assertThrows(RecordException.class, new FileRecordsManager(file)::load);
  }

  @Test
  void load_tooManyFields_throwsRecordException() throws Exception {
    write("EASY:10:20");
    assertThrows(RecordException.class, new FileRecordsManager(file)::load);
  }

  @Test
  void load_emptyValue_throwsRecordException() throws Exception {
    write("EASY:");
    assertThrows(RecordException.class, new FileRecordsManager(file)::load);
  }

  // ---- updateRecord ----

  @Test
  void updateRecord_higherPoints_replacesAndReturnsTrue() throws Exception {
    FileRecordsManager m = loaded("EASY:10");
    assertTrue(m.updateRecord(Level.EASY, 30));
    assertEquals(30, m.getRecord(Level.EASY));
  }

  @Test
  void updateRecord_lowerOrEqualPoints_keepsRecordAndReturnsFalse() throws Exception {
    FileRecordsManager m = loaded("EASY:10");
    assertFalse(m.updateRecord(Level.EASY, 5));
    assertFalse(m.updateRecord(Level.EASY, 10));
    assertEquals(10, m.getRecord(Level.EASY));
  }

  @Test
  void updateRecord_onlyAffectsThatLevel() throws Exception {
    FileRecordsManager m = loaded("EASY:10", "HARD:20");
    m.updateRecord(Level.EASY, 99);
    assertEquals(20, m.getRecord(Level.HARD));
    assertEquals(0, m.getRecord(Level.INSANE));
  }

  @Test
  void updateRecord_firstTimePlayedLevel_zeroPointsIsNotARecord() throws Exception {
    FileRecordsManager m = loaded();
    assertFalse(m.updateRecord(Level.INSANE, 0));
  }

  // ---- save ----

  @Test
  void save_writesOneLinePerLevel() throws Exception {
    FileRecordsManager m = loaded("EASY:10");
    m.updateRecord(Level.HARD, 40);
    m.save();

    List<String> lines = Files.readAllLines(file.toPath());
    assertEquals(Level.values().length, lines.size());
    assertTrue(lines.contains("EASY:10"));
    assertTrue(lines.contains("HARD:40"));
    assertTrue(lines.contains("INSANE:0"));
  }

  @Test
  void save_thenLoadInANewManager_roundTrips() throws Exception {
    FileRecordsManager first = loaded("EASY:10");
    first.updateRecord(Level.INSANE, 70);
    first.save();

    FileRecordsManager second = new FileRecordsManager(file);
    second.load();
    assertEquals(10, second.getRecord(Level.EASY));
    assertEquals(0, second.getRecord(Level.HARD));
    assertEquals(70, second.getRecord(Level.INSANE));
  }

  @Test
  void save_createsTheFileIfItDidNotExist() throws Exception {
    FileRecordsManager m = new FileRecordsManager(file);
    m.updateRecord(Level.EASY, 5);
    m.save();
    assertTrue(file.exists());
  }

  @Test
  void save_unwritablePath_throwsRecordException() {
    // the parent directory does not exist
    File bad = dir.resolve("missing").resolve("record.txt").toFile();
    FileRecordsManager m = new FileRecordsManager(bad);
    assertThrows(RecordException.class, m::save);
  }

  // ---- getRecord ----

  @Test
  void getRecord_beforeLoad_isZero() {
    FileRecordsManager m = new FileRecordsManager(file);
    for (Level level : Level.values())
      assertEquals(0, m.getRecord(level));
  }
}
