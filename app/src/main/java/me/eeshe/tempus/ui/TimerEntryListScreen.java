package me.eeshe.tempus.ui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
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

  public TimerEntryListScreen(TimerEntryService timerEntryService) {
    this.timerEntryService = timerEntryService;
  }

  public void open(DefaultTerminalFactory terminalFactory) {
    try {
      Screen screen = terminalFactory.createScreen();
      screen.startScreen();

      while (true) {
        screen.doResizeIfNecessary();
        displayTimeEntries(screen);
        screen.refresh();

        KeyStroke keyStroke = screen.pollInput();
        if (keyStroke == null) {
          continue;
        }
        if (keyStroke.getKeyType() == KeyType.Escape) {
          break;
        }
        CursorUtil.attemptCursorMovement(screen, keyStroke);
      }
      // WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
      // textGUI.setTheme(new SimpleTheme(
      // TextColor.ANSI.DEFAULT,
      // TextColor.ANSI.DEFAULT));
      //
      // BasicWindow window = new BasicWindow();
      // window.setHints(List.of(
      // Window.Hint.NO_DECORATIONS,
      // Window.Hint.FULL_SCREEN));
      //
      // Panel panel = new Panel(new GridLayout(4));
      //
      // List<TimerEntry> timerEntries = timerEntryService.fetchAll();
      // Set<LocalDate> listedDates = new HashSet<>();
      // for (TimerEntry timerEntry : timerEntries) {
      // LocalDateTime startDateTime = timerEntry.getStartDateTime();
      // LocalDate startDate = startDateTime.toLocalDate();
      // if (!listedDates.contains(startDate)) {
      // listedDates.add(startDate);
      // addDaySeparator(panel, timerEntry, timerEntries);
      // }
      // addTimerEntry(panel, timerEntry);
      // }
      // window.setComponent(panel);
      // textGUI.addWindowAndWait(window);
    } catch (IOException e) {
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
        .formatMillisecondsToHHMMSS(dailyTimerEntries.computeEllapsedTimeMillis());

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
      saveNextDisplayRow(displayRow, null);

      for (TimerEntry timerEntry : entrySet.getValue()) {
        addTimerEntry(screen, timerEntry);
      }
    }
  }

  private void addTimerEntry(Screen screen, TimerEntry timerEntry) {
    int displayRow = getNextDisplayRow();
    TextGraphics descriptionTextGraphics = screen.newTextGraphics().putString(
        2,
        displayRow,
        timerEntry.getDescription());
    TextGraphics billableTextGraphics = screen.newTextGraphics()
        .setForegroundColor(timerEntry.isBillable() ? TextColor.ANSI.GREEN : TextColor.ANSI.RED)
        .putString(
            50,
            displayRow,
            "$");

    long durationMillis = timerEntry.getDurationMillis();
    LocalDateTime startDateTime = timerEntry.getStartDateTime();
    LocalDateTime stopDateTime = startDateTime.plus(durationMillis, ChronoUnit.MILLIS);
    TextGraphics timestampsTextGraphics = screen.newTextGraphics().putString(
        80,
        displayRow,
        String.format("%s - %s",
            startDateTime.format(HOUR_MINUTE_FORMATTER),
            stopDateTime.format(HOUR_MINUTE_FORMATTER)));

    TextGraphics timeEllapsedTextGraphics = screen.newTextGraphics().putString(
        120,
        displayRow,
        TimeFormatUtil.formatMillisecondsToHHMMSS(durationMillis));

    saveNextDisplayRow(displayRow, timerEntry);
  }

  // private void addDaySeparator(Panel panel, TimerEntry addedTimerEntry,
  // List<TimerEntry> timerEntries) {
  // Label daySeparatorLabel = new
  // Label(addedTimerEntry.getStartDateTime().format(DAY_SEPARATOR_FORMATTER));
  // daySeparatorLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3));
  // panel.addComponent(daySeparatorLabel);
  //
  // LocalDate addedTimerEntryDate =
  // addedTimerEntry.getStartDateTime().toLocalDate();
  // long dayDurationMillis = 0L;
  // for (TimerEntry timerEntry : timerEntries) {
  // LocalDate timerEntryDate = timerEntry.getStartDateTime().toLocalDate();
  // if (!timerEntryDate.isEqual(addedTimerEntryDate)) {
  // if (timerEntryDate.isAfter(addedTimerEntryDate)) {
  // break;
  // }
  // continue;
  // }
  // dayDurationMillis += timerEntry.getDurationMillis();
  // }
  // panel.addComponent(new
  // Label(TimeFormatUtil.formatMillisecondsToHHMMSS(dayDurationMillis)));
  // }

  private int getNextDisplayRow() {
    if (displayedTimerEntries.isEmpty()) {
      return 0;
    }
    return displayedTimerEntries.lastKey() + 1;
  }

  private void saveNextDisplayRow(int displayRow, TimerEntry timerEntry) {
    displayedTimerEntries.put(displayRow, timerEntry);
  }

  // private void addTimerEntry(Panel panel, TimerEntry timerEntry) {
  // Panel projectTaskPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
  //
  // Label projectTaskLabel = new Label(timerEntry.generateProjectTaskString());
  // projectTaskLabel.setForegroundColor(TextColor.ANSI.MAGENTA);
  // projectTaskLabel.setPreferredSize(new TerminalSize(30, 1));
  // projectTaskPanel.addComponent(projectTaskLabel);
  //
  // Label descriptionLabel = new Label(timerEntry.getDescription());
  // projectTaskPanel.addComponent(descriptionLabel);
  //
  // panel.addComponent(projectTaskPanel);
  //
  // Label billableLabel = new Label("$");
  // billableLabel.setForegroundColor(timerEntry.isBillable() ?
  // TextColor.ANSI.GREEN : TextColor.ANSI.RED);
  // billableLabel.addStyle(SGR.BOLD);
  // panel.addComponent(billableLabel);
  //
  // long durationMillis = timerEntry.getDurationMillis();
  // LocalDateTime startDateTime = timerEntry.getStartDateTime();
  // LocalDateTime stopDateTime = startDateTime.plus(durationMillis,
  // ChronoUnit.MILLIS);
  // Label timesLabel = new Label(String.format("%s - %s",
  // startDateTime.format(HOUR_MINUTE_FORMATTER),
  // stopDateTime.format(HOUR_MINUTE_FORMATTER)));
  // panel.addComponent(timesLabel);
  //
  // Label durationLabel = new
  // Label(TimeFormatUtil.formatMillisecondsToHHMMSS(durationMillis));
  // panel.addComponent(durationLabel);
  // }
  //
  // private int getNextDisplayRow() {
  // if (displayedTimerEntries.isEmpty()) {
  // return 0;
  // }
  // return displayedTimerEntries.lastKey() + 1;
  // }
}
