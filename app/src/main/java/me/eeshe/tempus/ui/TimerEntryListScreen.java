package me.eeshe.tempus.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import me.eeshe.tempus.model.DailyTimerEntries;
import me.eeshe.tempus.model.TimerEntry;
import me.eeshe.tempus.service.TimerEntryService;
import me.eeshe.tempus.util.TimeFormatUtil;

public class TimerEntryListScreen {
  private static final DateTimeFormatter DAY_SEPARATOR_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d");
  private static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final TreeMap<Integer, TimerEntryListRow> listRows = new TreeMap<>();
  private final TimerEntryService timerEntryService;

  private TerminalPosition cursorPosition;
  private int scrolledRows;

  public TimerEntryListScreen(TimerEntryService timerEntryService) {
    this.timerEntryService = timerEntryService;
  }

  public void open(DefaultTerminalFactory terminalFactory) {
    try {
      Screen screen = terminalFactory.createScreen();
      screen.startScreen();

      displayTimeEntries(screen);
      while (true) {
        TerminalSize newTerminalSize = screen.doResizeIfNecessary();
        if (newTerminalSize != null) {
          screen.clear();
          displayTimeEntries(screen);
        }
        screen.refresh();

        KeyStroke keyStroke = screen.pollInput();
        if (keyStroke == null) {
          continue;
        }
        KeyType keyType = keyStroke.getKeyType();
        if (keyType == KeyType.Escape) {
          break;
        }
        if (keyType == KeyType.Character && Character.toLowerCase(keyStroke.getCharacter()) == 'n' &&
            !keyStroke.isCtrlDown()) {
          createTimerEntry(screen);
          continue;
        }
        if (keyType == KeyType.Enter || (keyType == KeyType.Character && keyStroke.getCharacter() == ' ')) {
          continueTimerEntry(screen);
          continue;
        }
        handleCursorMovement(screen, keyStroke);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void displayTimeEntries(Screen screen) {
    listRows.clear();

    computeTimerEntryLines(screen);
    drawTimerEntryLines(screen);
    drawFooter(screen);
  }

  private void computeTimerEntryLines(Screen screen) {
    Map<LocalDate, DailyTimerEntries> timerEntries = timerEntryService.fetchAllDaily();
    for (DailyTimerEntries dailyTimerEntries : timerEntries.values()) {
      addDaySeparator(screen, dailyTimerEntries);
      addTimerEntries(screen, dailyTimerEntries.getTimerEntries());
    }
  }

  private void addDaySeparator(Screen screen, DailyTimerEntries dailyTimerEntries) {
    int listRow = getNextListRow();
    TimerEntryListRow timerEntryListLine = new TimerEntryListRow(listRow, null);
    timerEntryListLine.addTerminalText()
        .setColumn(0)
        .setText(dailyTimerEntries.getDate().format(DAY_SEPARATOR_FORMATTER));

    String dayElapsedTimeString = TimeFormatUtil
        .formatMillisecondsToHHMMSS(dailyTimerEntries.computeElapsedTimeMillis());

    TerminalSize terminalSize = screen.getTerminalSize();
    timerEntryListLine.addTerminalText()
        .setColumn(terminalSize.getColumns() - dayElapsedTimeString.length())
        .setText(dayElapsedTimeString);

    saveListRow(timerEntryListLine);
  }

  private void addTimerEntries(Screen screen, Map<String, List<TimerEntry>> timerEntries) {
    for (Entry<String, List<TimerEntry>> entrySet : timerEntries.entrySet()) {
      String projectTaskName = entrySet.getKey();
      int listRow = getNextListRow();
      TimerEntryListRow timerEntryListRow = new TimerEntryListRow(listRow, null);
      timerEntryListRow.addTerminalText()
          .setColumn(0)
          .setForegroundColor(TextColor.ANSI.MAGENTA)
          .setText(projectTaskName);

      final TimerEntry firstTimerEntry = entrySet.getValue().get(0);
      final boolean matchTask = true;
      final long dailyElapsedTimeMillis = timerEntryService.computeDailyElapsedTimeMillis(
          firstTimerEntry,
          firstTimerEntry.getStartDateTime().toLocalDate(),
          matchTask);
      final String dailyElapsedTimeString = TimeFormatUtil.formatMillisecondsToHHMMSS(dailyElapsedTimeMillis);

      timerEntryListRow.addTerminalText()
          .setColumn(screen.getTerminalSize().getColumns() - dailyElapsedTimeString.length())
          .setForegroundColor(TextColor.ANSI.MAGENTA)
          .setText(dailyElapsedTimeString);

      saveListRow(timerEntryListRow);

      for (TimerEntry timerEntry : entrySet.getValue()) {
        addTimerEntry(screen, timerEntry);
      }
    }
  }

  private void addTimerEntry(Screen screen, TimerEntry timerEntry) {
    long durationMillis = timerEntry.getDurationMillis();
    LocalDateTime startDateTime = timerEntry.getStartDateTime();
    LocalDateTime stopDateTime = startDateTime.plus(durationMillis, ChronoUnit.MILLIS);

    final String descriptionString = timerEntry.getDescription();
    final String billableString = "$";
    final String timestampsString = String.format("%s - %s",
        startDateTime.format(HOUR_MINUTE_FORMATTER),
        stopDateTime.format(HOUR_MINUTE_FORMATTER));
    final String timeElapsedString = TimeFormatUtil.formatMillisecondsToHHMMSS(durationMillis);

    int listRow = getNextListRow();
    TimerEntryListRow timerEntryListRow = new TimerEntryListRow(listRow, timerEntry);
    timerEntryListRow.addTerminalText()
        .setColumn(2)
        .setText(descriptionString);

    final int totalRightSizeLength = billableString.length() + timestampsString.length() +
        timeElapsedString.length() + 4; // +4 for separation
    int newTextColumn = screen.getTerminalSize().getColumns() - totalRightSizeLength;
    timerEntryListRow.addTerminalText()
        .setColumn(newTextColumn)
        .setForegroundColor(timerEntry.isBillable() ? TextColor.ANSI.GREEN : TextColor.ANSI.RED)
        .setText(billableString);

    newTextColumn += billableString.length() + 2;
    timerEntryListRow.addTerminalText()
        .setColumn(newTextColumn)
        .setText(timestampsString);

    newTextColumn += timestampsString.length() + 2;
    timerEntryListRow.addTerminalText()
        .setColumn(newTextColumn)
        .setText(timeElapsedString);

    saveListRow(timerEntryListRow);
  }

  private void drawTimerEntryLines(Screen screen) {
    TerminalSize terminalSize = screen.getTerminalSize();
    for (int row = 0; row < terminalSize.getRows(); row++) {
      TimerEntryListRow timerEntryListRow = listRows.get(row + scrolledRows);
      if (timerEntryListRow == null) {
        continue;
      }
      timerEntryListRow.draw(screen, row);
    }
  }

  private void drawFooter(Screen screen) {
    TerminalSize terminalSize = screen.getTerminalSize();
    int height = terminalSize.getRows() - 1;
    int width = terminalSize.getColumns();

    final TerminalPosition from = new TerminalPosition(0, height);
    final TerminalPosition to = new TerminalPosition(width, height);
    final TextColor backgroundColor = TextColor.ANSI.MAGENTA_BRIGHT;
    screen.newTextGraphics().drawLine(
        from,
        to,
        TextCharacter.DEFAULT_CHARACTER
            .withCharacter(' ')
            .withBackgroundColor(backgroundColor));

    final String text = "N: New Timer  Space/Enter: Continue Timer";
    screen.newTextGraphics().setBackgroundColor(backgroundColor).putString(from, text);
  }

  private void createTimerEntry(Screen screen) {
    storeCursorPosition(screen);
    new TimeTrackerScreen().open(screen);
    displayTimeEntries(screen);
    restoreCursorPosition(screen);
  }

  private void continueTimerEntry(Screen screen) {
    TimerEntry timerEntry = getRowTimerEntry(screen.getCursorPosition().getRow());
    if (timerEntry == null) {
      return;
    }
    storeCursorPosition(screen);
    new TimeTrackerScreen(timerEntry).open(screen);
    displayTimeEntries(screen);
    restoreCursorPosition(screen);
  }

  private void handleCursorMovement(Screen screen, KeyStroke keyStroke) {
    if (screen == null) {
      return;
    }
    if (keyStroke == null) {
      return;
    }
    switch (keyStroke.getKeyType()) {
      case ArrowUp -> {
        moveCursorUp(screen);
      }
      case ArrowDown -> {
        moveCursorDown(screen);
      }
      case ArrowLeft -> {
        moveCursorLeft(screen);
      }
      case ArrowRight -> {
        moveCursorRight(screen);
      }
      case Character -> {
        char character = keyStroke.getCharacter();
        switch (character) {
          case 'h' -> {
            moveCursorLeft(screen);
          }
          case 'j' -> {
            moveCursorDown(screen);
          }
          case 'k' -> {
            moveCursorUp(screen);
          }
          case 'l' -> {
            moveCursorRight(screen);
          }
        }
      }
      default -> {

      }
    }
  }

  private void moveCursorUp(Screen screen) {
    TerminalPosition cursorPosition = screen.getCursorPosition();
    int cursorRow = cursorPosition.getRow();
    if (cursorRow + scrolledRows == listRows.firstKey()) {
      return;
    }
    if (cursorRow > 0) {
      screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1));
      return;
    }
    scrolledRows -= 1;
    screen.clear();
    displayTimeEntries(screen);
  }

  private void moveCursorDown(Screen screen) {
    TerminalPosition cursorPosition = screen.getCursorPosition();
    int cursorRow = cursorPosition.getRow();
    if (cursorRow + scrolledRows == listRows.lastKey()) {
      return;
    }
    int listHeight = screen.getTerminalSize().getRows() - 2; // Account for footer and index 0
    if (cursorRow < listHeight) {
      screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1));
      return;
    }
    scrolledRows += 1;
    screen.clear();
    displayTimeEntries(screen);
  }

  private void moveCursorLeft(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(-1));
  }

  private void moveCursorRight(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(1));
  }

  private int getNextListRow() {
    if (listRows.isEmpty()) {
      return 0;
    }
    return listRows.lastKey() + 1;
  }

  private void saveListRow(TimerEntryListRow timerEntryListLine) {
    listRows.put(getNextListRow(), timerEntryListLine);
  }

  private TimerEntry getRowTimerEntry(int row) {
    TimerEntryListRow timerEntryListRow = listRows.get(row + scrolledRows);
    if (timerEntryListRow == null) {
      return null;
    }
    return timerEntryListRow.getTimerEntry();
  }

  private void storeCursorPosition(Screen screen) {
    this.cursorPosition = screen.getCursorPosition();
  }

  private void restoreCursorPosition(Screen screen) {
    if (cursorPosition == null) {
      return;
    }
    screen.setCursorPosition(cursorPosition);
  }
}

class TimerEntryListRow {
  private final int row;
  private final TimerEntry timerEntry;
  private final List<TerminalText> terminalTexts;

  public TimerEntryListRow(int row, TimerEntry timerEntry) {
    this.row = row;
    this.timerEntry = timerEntry;
    this.terminalTexts = new ArrayList<>();
  }

  public void draw(Screen screen, int row) {
    for (TerminalText terminalText : terminalTexts) {
      screen.newTextGraphics()
          .setBackgroundColor(terminalText.getBackgroundColor())
          .setForegroundColor(terminalText.getForegroundColor())
          .putString(
              terminalText.computeTerminalPosition(row),
              terminalText.getText());
    }
  }

  public int getRow() {
    return row;
  }

  public TimerEntry getTimerEntry() {
    return timerEntry;
  }

  public List<TerminalText> getTerminalTexts() {
    return terminalTexts;
  }

  public TerminalText addTerminalText() {
    TerminalText terminalText = new TerminalText();
    terminalTexts.add(terminalText);

    return terminalText;
  }
}

class TerminalText {
  private int column;
  private TextColor backgroundColor;
  private TextColor foregroundColor;
  private String text;

  public TerminalPosition computeTerminalPosition(int row) {
    return new TerminalPosition(column, row);
  }

  public int getColumn() {
    return column;
  }

  public TerminalText setColumn(int column) {
    this.column = column;
    return this;
  }

  public TextColor getBackgroundColor() {
    return backgroundColor;
  }

  public TerminalText setBackgroundColor(TextColor backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }

  public TextColor getForegroundColor() {
    return foregroundColor;
  }

  public TerminalText setForegroundColor(TextColor foregroundColor) {
    this.foregroundColor = foregroundColor;
    return this;
  }

  public String getText() {
    return text;
  }

  public TerminalText setText(String text) {
    this.text = text;
    return this;
  }
}
