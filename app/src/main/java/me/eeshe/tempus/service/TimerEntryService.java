package me.eeshe.tempus.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.tempus.database.SQLiteManager;
import me.eeshe.tempus.model.DailyTimerEntries;
import me.eeshe.tempus.model.TimerEntry;

public class TimerEntryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimerEntryService.class);
  private static final String TIMER_ENTRY_TABLE = "TimerEntry";

  private final SQLiteManager sqLiteManager;

  public TimerEntryService(SQLiteManager sqLiteManager) {
    this.sqLiteManager = sqLiteManager;

    createTables();
  }

  /**
   * Creates all the necessary tables for TimerEntries.
   */
  private void createTables() {
    createTimerEntriesTable();
  }

  /**
   * Creates the table that will store TimeEntry data.
   */
  private void createTimerEntriesTable() {
    createTable("CREATE TABLE IF NOT EXISTS " + TIMER_ENTRY_TABLE + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "projectName TEXT NOT NULL, " +
        "clientName TEXT, " +
        "description TEXT, " +
        "task TEXT, " +
        "email TEXT, " +
        "tags TEXT, " +
        "billable INTEGER NOT NULL, " +
        "startTimeMillis INTEGER NOT NULL, " +
        "durationMillis INTEGER NOT NULL" +
        ")");
  }

  /**
   * Creates a SQLite table with the passed SQL statement.
   *
   * @param createTableSql SQL Statement to execute.
   */
  private void createTable(String createTableSql) {
    try (Connection connection = sqLiteManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Error creating table {}. Message: {}", TIMER_ENTRY_TABLE, e.getMessage());
    }
  }

  /**
   * Saves the passed TimerEntry to the database.
   *
   * @param timerEntry TimerEntry to save.
   */
  public void save(TimerEntry timerEntry) {
    final String sql = "INSERT INTO " + TIMER_ENTRY_TABLE +
        " (projectName, clientName, description, task, email, tags, billable, startTimeMillis, durationMillis) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection connection = sqLiteManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, timerEntry.getProjectName());
      preparedStatement.setString(2, timerEntry.getClientName());
      preparedStatement.setString(3, timerEntry.getDescription());
      preparedStatement.setString(4, timerEntry.getTask());
      preparedStatement.setString(5, timerEntry.getEmail());
      preparedStatement.setString(6, String.join(", ", timerEntry.getTags()));
      preparedStatement.setBoolean(7, timerEntry.isBillable());
      preparedStatement.setLong(8, timerEntry.getStartTimeMillis());
      preparedStatement.setLong(9, timerEntry.getDurationMillis());

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Error saving TimeEntry. Error: {}", e.getMessage());
    }
  }

  public Map<LocalDate, DailyTimerEntries> fetchAllDaily() {
    Map<LocalDate, DailyTimerEntries> timerEntries = new LinkedHashMap<>();
    for (TimerEntry timerEntry : fetchAll()) {
      LocalDate timerEntryDate = timerEntry.getStartDateTime().toLocalDate();
      DailyTimerEntries dailyTimerEntries = timerEntries.getOrDefault(timerEntryDate,
          new DailyTimerEntries(timerEntryDate));

      String projectTaskString = timerEntry.createProjectTaskString();
      List<TimerEntry> groupedTimerEntries = dailyTimerEntries.getTimerEntries().getOrDefault(
          projectTaskString, new ArrayList<>());
      groupedTimerEntries.add(timerEntry);

      dailyTimerEntries.getTimerEntries().put(projectTaskString, groupedTimerEntries);

      timerEntries.put(timerEntryDate, dailyTimerEntries);
    }
    return timerEntries;
  }

  /**
   * Fetches all the stored TimerEntries sorted by their start time.
   *
   * @return Stored TimerEntries.
   */
  public List<TimerEntry> fetchAll() {
    final String sql = "SELECT * FROM " + TIMER_ENTRY_TABLE + " ORDER BY startTimeMillis DESC";
    List<TimerEntry> timerEntries = new ArrayList<>();
    try (Connection connection = sqLiteManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        TimerEntry timerEntry = parseTimerEntry(resultSet);
        if (timerEntry == null) {
          continue;
        }
        timerEntries.add(timerEntry);
      }
    } catch (SQLException e) {
      LOGGER.error("Error fetching all TimerEntries. Message: {}", e.getMessage());
    }
    return timerEntries;
  }

  /**
   * Computes the amount of time in milliseconds a Project has been elapsed for
   * within the passed Date.
   *
   * @param timerEntry TimerEntry whose project will be computed.
   * @param localDate  Date to compute.
   * @param matchTask  Whether the TimerEntry task should match as well.
   * @return Total elapsed time in milliseconds of the passed TimerEntry's
   *         project.
   */
  public long computeDailyElapsedTimeMillis(TimerEntry timerEntry, LocalDate localDate, boolean matchTask) {
    return fetch(timerEntry, LocalDate.now(), matchTask)
        .stream().map(TimerEntry::getDurationMillis).mapToLong(Long::longValue).sum();
  }

  /**
   *
   * Fetches all the TimerEntries matching the passed TimerEntry's project
   * name that are within the passed date.
   *
   * @param timerEntry TimerEntry to compare.
   * @param matchTask  Whether the TimerEntry task should match as well.
   * @return Matching TimerEntries.
   */
  private List<TimerEntry> fetch(TimerEntry timerEntry, LocalDate date, boolean matchTask) {
    List<TimerEntry> timerEntries = new ArrayList<>();
    String sql = "SELECT * FROM " + TIMER_ENTRY_TABLE + " WHERE projectName = ? AND " +
        "startTimeMillis BETWEEN ? AND ?";
    if (matchTask) {
      sql += " AND COALESCE(task, '') = ?";
    }
    try (Connection connection = sqLiteManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, timerEntry.getProjectName());

      ZoneId zoneId = ZoneId.systemDefault();
      preparedStatement.setLong(2, date.atStartOfDay(zoneId).toInstant().toEpochMilli());
      preparedStatement.setLong(3, date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli());
      if (matchTask) {
        final String task = timerEntry.getTask() == null ? "" : timerEntry.getTask();
        preparedStatement.setString(4, task);
      }

      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        TimerEntry dateTimerEntry = parseTimerEntry(resultSet);
        if (dateTimerEntry == null) {
          continue;
        }
        timerEntries.add(dateTimerEntry);
      }
    } catch (SQLException e) {
      LOGGER.error("Error fetching TimeEntries matching {} and date {}. Message: {}",
          timerEntry.getProjectName(),
          date,
          e.getMessage());
    }
    return timerEntries;
  }

  private TimerEntry parseTimerEntry(ResultSet resultSet) {
    try {
      String projectName = resultSet.getString("projectName");
      String clientName = resultSet.getString("clientName");
      String description = resultSet.getString("description");
      String task = resultSet.getString("task");
      String email = resultSet.getString("email");
      List<String> tags = Arrays.asList(resultSet.getString("tags").split(","));
      boolean billable = resultSet.getBoolean("billable");
      long startTimeMillis = resultSet.getLong("startTimeMillis");
      long durationMillis = resultSet.getLong("durationMillis");

      return new TimerEntry(
          projectName,
          clientName,
          description,
          task,
          email,
          tags,
          billable,
          startTimeMillis,
          durationMillis);
    } catch (SQLException e) {
      LOGGER.error("Error parsing TimerEntry. Message: {}", e.getMessage());
    }
    return null;
  }
}
