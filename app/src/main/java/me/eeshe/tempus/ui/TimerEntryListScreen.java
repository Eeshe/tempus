package me.eeshe.tempus.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import me.eeshe.tempus.model.DailyTimerEntries;
import me.eeshe.tempus.model.TimerEntry;
import me.eeshe.tempus.service.TimerEntryService;
import me.eeshe.tempus.util.CursorUtil;
import me.eeshe.tempus.util.TimeFormatUtil;

public class TimerEntryListScreen {
  private static final DateTimeFormatter DAY_SEPARATOR_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d");
  private static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final TreeMap<Integer, TimerEntry> displayedTimerEntries = new TreeMap<>();
  private final TimerEntryService timerEntryService;

  private TerminalPosition cursorPosition;

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
        if (keyType == KeyType.Character && Character.toLowerCase(keyStroke.getCharacter()) == 'n') {
          createTimerEntry(screen);
          continue;
        }
        if (keyType == KeyType.Enter || (keyType == KeyType.Character && keyStroke.getCharacter() == ' ')) {
          continueTimerEntry(screen);
          continue;
        }
        CursorUtil.attemptCursorMovement(screen, keyStroke);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void displayTimeEntries(Screen screen) {
    displayedTimerEntries.clear();

    Map<LocalDate, DailyTimerEntries> timerEntries = timerEntryService.fetchAllDaily();
    for (DailyTimerEntries dailyTimerEntries : timerEntries.values()) {
      addDaySeparator(screen, dailyTimerEntries);
      addTimerEntries(screen, dailyTimerEntries.getTimerEntries());
    }
  }

  private void addDaySeparator(Screen screen, DailyTimerEntries dailyTimerEntries) {
    int displayRow = getNextDisplayRow();
    TerminalSize terminalSize = screen.getTerminalSize();
    screen.newTextGraphics().putString(
        0,
        displayRow,
        dailyTimerEntries.getDate().format(DAY_SEPARATOR_FORMATTER));

    String dayElapsedTimeString = TimeFormatUtil
        .formatMillisecondsToHHMMSS(dailyTimerEntries.computeElapsedTimeMillis());

    screen.newTextGraphics().putString(
        terminalSize.getColumns() - dayElapsedTimeString.length(),
        displayRow,
        dayElapsedTimeString);

    saveNextDisplayRow(displayRow, null);
  }

  private void addTimerEntries(Screen screen, Map<String, List<TimerEntry>> timerEntries) {
    for (Entry<String, List<TimerEntry>> entrySet : timerEntries.entrySet()) {
      String projectTaskName = entrySet.getKey();
      int displayRow = getNextDisplayRow();
      screen.newTextGraphics().setForegroundColor(TextColor.ANSI.MAGENTA).putString(
          0,
          displayRow,
          projectTaskName);

      final TimerEntry firstTimerEntry = entrySet.getValue().get(0);
      final long dailyElapsedTimeMillis = timerEntryService.computeDailyElapsedTimeMillis(
          firstTimerEntry,
          firstTimerEntry.getStartDateTime().toLocalDate());
      final String dailyElapsedTimeString = TimeFormatUtil.formatMillisecondsToHHMMSS(dailyElapsedTimeMillis);
      screen.newTextGraphics().setForegroundColor(TextColor.ANSI.MAGENTA).putString(
          screen.getTerminalSize().getColumns() - dailyElapsedTimeString.length(),
          displayRow,
          dailyElapsedTimeString);
      saveNextDisplayRow(displayRow, null);

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

    int displayRow = getNextDisplayRow();
    screen.newTextGraphics().putString(
        2,
        displayRow,
        descriptionString);

    final int totalRightSizeLength = billableString.length() + timestampsString.length() +
        timeElapsedString.length() + 4; // +4 for separation
    int newTextColumn = screen.getTerminalSize().getColumns() - totalRightSizeLength;
    screen.newTextGraphics()
        .setForegroundColor(timerEntry.isBillable() ? TextColor.ANSI.GREEN : TextColor.ANSI.RED)
        .putString(
            newTextColumn,
            displayRow,
            billableString);

    newTextColumn += billableString.length() + 2;
    screen.newTextGraphics().putString(
        newTextColumn,
        displayRow,
        timestampsString);

    newTextColumn += timestampsString.length() + 2;
    screen.newTextGraphics().putString(
        newTextColumn,
        displayRow,
        timeElapsedString);

    saveNextDisplayRow(displayRow, timerEntry);
  }

  private void createTimerEntry(Screen screen) {
    storeCursorPosition(screen);
    new TimeTrackerScreen().open(screen);
    displayTimeEntries(screen);
    restoreCursorPosition(screen);
  }

  private void continueTimerEntry(Screen screen) {
    TimerEntry timerEntry = getDisplayedTimerEntry(screen.getCursorPosition().getRow());
    if (timerEntry == null) {
      return;
    }
    storeCursorPosition(screen);
    new TimeTrackerScreen(timerEntry).open(screen);
    displayTimeEntries(screen);
    restoreCursorPosition(screen);
  }

  private int getNextDisplayRow() {
    if (displayedTimerEntries.isEmpty()) {
      return 0;
    }
    return displayedTimerEntries.lastKey() + 1;
  }

  private TimerEntry getDisplayedTimerEntry(int row) {
    return displayedTimerEntries.get(row);
  }

  private void saveNextDisplayRow(int displayRow, TimerEntry timerEntry) {
    displayedTimerEntries.put(displayRow, timerEntry);
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
