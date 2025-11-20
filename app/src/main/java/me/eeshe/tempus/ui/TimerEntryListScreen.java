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
import com.googlecode.lanterna.graphics.TextGraphics;
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
    final int listRow = getNextListRow();
    final TimerEntry timerEntry = null;
    final boolean isProjectRow = false;
    final TimerEntryListRow timerEntryListLine = new TimerEntryListRow(listRow, timerEntry, isProjectRow);
    final TextColor backgroundColor = TextColor.ANSI.WHITE;
    final TextColor foregroundColor = TextColor.ANSI.BLACK;
    timerEntryListLine.addTerminalLine()
        .setFrom(new TerminalPosition(0, listRow))
        .setTo(new TerminalPosition(screen.getTerminalSize().getColumns(), listRow))
        .setCharacter(' ')
        .setBackgroundColor(backgroundColor);

    timerEntryListLine.addTerminalText()
        .setColumn(0)
        .setText(dailyTimerEntries.getDate().format(DAY_SEPARATOR_FORMATTER))
        .setBackgroundColor(backgroundColor)
        .setForegroundColor(foregroundColor);

    String dayElapsedTimeString = TimeFormatUtil
        .formatMillisecondsToHHMMSS(dailyTimerEntries.computeElapsedTimeMillis());

    TerminalSize terminalSize = screen.getTerminalSize();
    timerEntryListLine.addTerminalText()
        .setColumn(terminalSize.getColumns() - dayElapsedTimeString.length())
        .setText(dayElapsedTimeString)
        .setBackgroundColor(backgroundColor)
        .setForegroundColor(foregroundColor);

    saveListRow(timerEntryListLine);
  }

  private void addTimerEntries(Screen screen, Map<String, List<TimerEntry>> timerEntries) {
    for (Entry<String, List<TimerEntry>> entrySet : timerEntries.entrySet()) {
      final String projectTaskName = entrySet.getKey();
      final int listRow = getNextListRow();
      final TimerEntry timerEntry = null;
      final boolean isProjectRow = true;
      final TimerEntryListRow timerEntryListRow = new TimerEntryListRow(listRow, timerEntry, isProjectRow);
      final TextColor backgroundColor = new TextColor.RGB(132, 130, 143);
      final TextColor foregroundColor = TextColor.ANSI.BLACK;
      timerEntryListRow.addTerminalLine()
          .setFrom(new TerminalPosition(0, listRow))
          .setTo(new TerminalPosition(screen.getTerminalSize().getColumns(), listRow))
          .setCharacter(' ')
          .setBackgroundColor(backgroundColor);
      timerEntryListRow.addTerminalText()
          .setColumn(0)
          .setText(projectTaskName)
          .setBackgroundColor(backgroundColor)
          .setForegroundColor(foregroundColor);

      final TimerEntry firstTimerEntry = entrySet.getValue().get(0);
      final boolean matchTask = true;
      final long dailyElapsedTimeMillis = timerEntryService.computeDailyElapsedTimeMillis(
          firstTimerEntry,
          firstTimerEntry.getStartDateTime().toLocalDate(),
          matchTask);
      final String dailyElapsedTimeString = TimeFormatUtil.formatMillisecondsToHHMMSS(dailyElapsedTimeMillis);

      timerEntryListRow.addTerminalText()
          .setColumn(screen.getTerminalSize().getColumns() - dailyElapsedTimeString.length())
          .setBackgroundColor(backgroundColor)
          .setForegroundColor(foregroundColor)
          .setText(dailyElapsedTimeString);

      saveListRow(timerEntryListRow);

      for (TimerEntry projectTimerEntry : entrySet.getValue()) {
        addTimerEntry(screen, projectTimerEntry);
      }
    }
  }

  private void addTimerEntry(Screen screen, TimerEntry timerEntry) {
    final long durationMillis = timerEntry.getDurationMillis();
    final LocalDateTime startDateTime = timerEntry.getStartDateTime();
    final LocalDateTime stopDateTime = startDateTime.plus(durationMillis, ChronoUnit.MILLIS);

    final String descriptionString = timerEntry.getDescription();
    final String billableString = "$";
    final String timestampsString = String.format("%s - %s",
        startDateTime.format(HOUR_MINUTE_FORMATTER),
        stopDateTime.format(HOUR_MINUTE_FORMATTER));
    final String timeElapsedString = TimeFormatUtil.formatMillisecondsToHHMMSS(durationMillis);

    final int listRow = getNextListRow();
    final boolean isProjectRow = false;
    final TimerEntryListRow timerEntryListRow = new TimerEntryListRow(listRow, timerEntry, isProjectRow);
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
    final TerminalSize terminalSize = screen.getTerminalSize();
    for (int row = 0; row < terminalSize.getRows(); row++) {
      final TimerEntryListRow timerEntryListRow = listRows.get(row + scrolledRows);
      if (timerEntryListRow == null) {
        continue;
      }
      timerEntryListRow.draw(screen, row);
    }
  }

  private void drawFooter(Screen screen) {
    final TerminalSize terminalSize = screen.getTerminalSize();
    final int height = terminalSize.getRows() - 1;
    final int width = terminalSize.getColumns();

    final TerminalPosition from = new TerminalPosition(0, height);
    final TerminalPosition to = new TerminalPosition(width, height);
    final TextColor backgroundColor = TextColor.ANSI.WHITE;
    final TextColor foregroundColor = TextColor.ANSI.BLACK;
    screen.newTextGraphics().drawLine(
        from,
        to,
        TextCharacter.DEFAULT_CHARACTER
            .withCharacter(' ')
            .withBackgroundColor(backgroundColor));

    final String text = "N: New Timer  Space/Enter: Continue Timer  Ctrl + N/P: Nagivate Projects";
    screen.newTextGraphics()
        .setBackgroundColor(backgroundColor)
        .setForegroundColor(foregroundColor)
        .putString(from, text);
  }

  private void createTimerEntry(Screen screen) {
    storeCursorPosition(screen);
    new TimeTrackerScreen().open(screen);
    displayTimeEntries(screen);
    restoreCursorPosition(screen);
  }

  private void continueTimerEntry(Screen screen) {
    final TimerEntry timerEntry = getRowTimerEntry(screen.getCursorPosition().getRow());
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
        char character = Character.toLowerCase(keyStroke.getCharacter());
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
          case 'n', 'p' -> {
            navigateToNextProject(screen, keyStroke);
          }
        }
      }
      default -> {

      }
    }
  }

  private void moveCursorUp(Screen screen) {
    final TerminalPosition cursorPosition = screen.getCursorPosition();
    final int cursorRow = cursorPosition.getRow();
    if (cursorRow + scrolledRows == listRows.firstKey()) {
      return;
    }
    if (cursorRow > 0) {
      screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1));
      return;
    }
    final int scrolledRows = -1;
    scrollScreen(screen, scrolledRows);
  }

  private void moveCursorDown(Screen screen) {
    final TerminalPosition cursorPosition = screen.getCursorPosition();
    final int cursorRow = cursorPosition.getRow();
    if (cursorRow + scrolledRows == listRows.lastKey()) {
      return;
    }
    final int listHeight = screen.getTerminalSize().getRows() - 2; // Account for footer and index 0
    if (cursorRow < listHeight) {
      screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1));
      return;
    }
    final int scrolledRows = 1;
    scrollScreen(screen, scrolledRows);
  }

  private void scrollScreen(Screen screen, int rows) {
    scrolledRows += rows;
    screen.clear();
    displayTimeEntries(screen);
  }

  private void moveCursorLeft(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(-1));
  }

  private void moveCursorRight(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(1));
  }

  private void navigateToNextProject(Screen screen, KeyStroke keyStroke) {
    if (!keyStroke.isCtrlDown()) {
      return;
    }
    final int iterationIncrement = keyStroke.getCharacter() == 'n' ? 1 : -1;
    int cursorRow = screen.getCursorPosition().getRow() + iterationIncrement;
    boolean foundProjectLine = false;
    while (true) {
      TimerEntryListRow timerEntryListRow = listRows.get(cursorRow + scrolledRows);
      if (timerEntryListRow == null) {
        break;
      }
      if (!timerEntryListRow.isProjectRow()) {
        cursorRow += iterationIncrement;
        continue;
      }
      foundProjectLine = true;
      break;
    }
    if (!foundProjectLine) {
      return;
    }
    final TerminalSize terminalSize = screen.getTerminalSize();
    final int screenRows = terminalSize.getRows() - 1; // -1 to account for the footer
    if (cursorRow < 0) {
      // Found project is above the current top row
      scrollScreen(screen, cursorRow);
    } else if (cursorRow >= screenRows) {
      // Found project is below the current bottom row
      scrollScreen(screen, cursorRow - screenRows + 1); // +1 to account for the footer
      cursorRow = screenRows - 1; // -1 to account for the footer
    }
    screen.setCursorPosition(new TerminalPosition(
        screen.getCursorPosition().getColumn(),
        cursorRow));
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
    final TimerEntryListRow timerEntryListRow = listRows.get(row + scrolledRows);
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
  private final List<TerminalLine> terminalLines;
  private final List<TerminalText> terminalTexts;
  private final boolean isProjectRow;

  public TimerEntryListRow(int row, TimerEntry timerEntry, boolean isProjectRow) {
    this.row = row;
    this.timerEntry = timerEntry;
    this.terminalLines = new ArrayList<>();
    this.terminalTexts = new ArrayList<>();
    this.isProjectRow = isProjectRow;
  }

  public void draw(Screen screen, int row) {
    final TextGraphics textGraphics = screen.newTextGraphics();
    for (TerminalLine terminalLine : terminalLines) {
      textGraphics.drawLine(
          terminalLine.getFrom().withRow(row),
          terminalLine.getTo().withRow(row),
          TextCharacter.DEFAULT_CHARACTER
              .withCharacter(' ')
              .withBackgroundColor(terminalLine.getBackgroundColor())
              .withForegroundColor(terminalLine.getForegroundColor()));
    }
    for (TerminalText terminalText : terminalTexts) {
      textGraphics
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

  public List<TerminalLine> getTerminalLines() {
    return terminalLines;
  }

  public TerminalLine addTerminalLine() {
    final TerminalLine terminalLine = new TerminalLine();
    terminalLines.add(terminalLine);

    return terminalLine;
  }

  public List<TerminalText> getTerminalTexts() {
    return terminalTexts;
  }

  public TerminalText addTerminalText() {
    final TerminalText terminalText = new TerminalText();
    terminalTexts.add(terminalText);

    return terminalText;
  }

  public boolean isProjectRow() {
    return isProjectRow;
  }
}

class TerminalLine {
  private TerminalPosition from;
  private TerminalPosition to;
  private TextColor backgroundColor;
  private TextColor foregroundColor;
  private Character character;

  public TerminalPosition getFrom() {
    return from;
  }

  public TerminalLine setFrom(TerminalPosition from) {
    this.from = from;

    return this;
  }

  public TerminalPosition getTo() {
    return to;
  }

  public TerminalLine setTo(TerminalPosition to) {
    this.to = to;

    return this;
  }

  public TextColor getBackgroundColor() {
    return backgroundColor;
  }

  public TerminalLine setBackgroundColor(TextColor backgroundColor) {
    this.backgroundColor = backgroundColor;

    return this;
  }

  public TextColor getForegroundColor() {
    return foregroundColor;
  }

  public TerminalLine setForegroundColor(TextColor foregroundColor) {
    this.foregroundColor = foregroundColor;

    return this;
  }

  public Character getCharacter() {
    return character;
  }

  public TerminalLine setCharacter(Character character) {
    this.character = character;

    return this;
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
