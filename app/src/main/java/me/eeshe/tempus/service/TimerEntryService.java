package me.eeshe.tempus.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.eeshe.tempus.database.SQLiteManager;
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
        "startTimeMillis TEXT NOT NULL, " +
        "durationMillis INTEGER NOT NULL" +
        ")");
  }

  /**
   * Creates a SQLite table with the passed SQL statement.
   *
   * @param createTableSql SQL Statement to execute.
   */
  private void createTable(String createTableSql) {
    sqLiteManager.connect();
    try (Connection connection = sqLiteManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    sqLiteManager.close();
  }

  /**
   * Saves the passed TimerEntry to the database.
   *
   * @param timerEntry TimerEntry to save.
   */
  public void save(TimerEntry timerEntry) {
    sqLiteManager.connect();
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
      preparedStatement.setLong(8, timerEntry.getStartTimeMilliseconds());
      preparedStatement.setLong(9, timerEntry.getDurationMillis());

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Error saving TimeEntry. Error: {}", e.getMessage());
    }
    sqLiteManager.close();
  }
}
