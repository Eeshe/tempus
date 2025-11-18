package me.eeshe.tempus.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteManager.class);
  private static final String DATABASE_PATH = "database.db";
  private Connection connection; // The active database connection

  /**
   * Establishes a connection to the SQLite database defined by DATABASE_PATH.
   * If the database file does not exist, it will be created.
   * Any errors encountered (e.g., file system, driver, SQL) will be logged
   * and the method will simply return without establishing a connection.
   */
  public void connect() {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          LOGGER.info("Database connection already open for: {}", DATABASE_PATH);
          return; // Connection is already active, nothing to do
        }
      } catch (SQLException e) {
        LOGGER.warn(
            "Failed to check if existing connection is closed for {}. Attempting to reconnect anyway. Error: {}",
            DATABASE_PATH, e.getMessage());
        // Fall through to attempt reconnection
      }
    }

    // Ensure parent directories exist
    File dbFile = new File(DATABASE_PATH);
    File parentDir = dbFile.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      LOGGER.info("Creating parent directories for database at: {}", parentDir.getAbsolutePath());
      if (!parentDir.mkdirs()) {
        // If directory creation fails, log and return. No connection will be made.
        LOGGER.error("Failed to create parent directories for database path: {}", DATABASE_PATH);
        return;
      }
    }

    // JDBC URL for SQLite
    String url = "jdbc:sqlite:" + DATABASE_PATH;

    try {
      // Load the JDBC driver (optional for modern JDBC, but good practice)
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection(url);
      connection.setAutoCommit(true); // Default to auto-commit, repositories can manage transactions if needed
      LOGGER.info("Connected to SQLite database: {}", DATABASE_PATH);
    } catch (ClassNotFoundException e) {
      // This is a critical error; cannot connect without the driver. Log and return.
      LOGGER.error("SQLite JDBC driver not found. Please ensure 'sqlite-jdbc' is in your classpath.", e);
      connection = null; // Ensure connection is null if setup failed
    } catch (SQLException e) {
      // General SQL connection error. Log and return.
      LOGGER.error("Failed to connect to SQLite database: {}", DATABASE_PATH, e);
      connection = null; // Ensure connection is null if setup failed
    }
  }

  /**
   * Returns the active database connection.
   *
   * @return The active object, or null if no
   *         connection is open or an error occurred.
   */
  public Connection getConnection() {
    connect();
    try {
      if (connection == null || connection.isClosed()) {
        // This indicates a missing or closed connection. Log and return null.
        LOGGER.error("Attempted to get connection when it's not open for {}. Call connect() first.", DATABASE_PATH);
        return null;
      }
    } catch (SQLException e) {
      // An error occurred while trying to check if the connection is closed. Log and
      // return null.
      LOGGER.error("Failed to check connection status for {}.", DATABASE_PATH, e);
      return null;
    }
    return connection;
  }

  /**
   * Closes the database connection.
   */
  public void close() {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          connection.close();
          LOGGER.info("Disconnected from SQLite database: {}", DATABASE_PATH);
        }
      } catch (SQLException e) {
        // Error closing the connection. Log but do not re-throw.
        LOGGER.error("Error closing SQLite connection for {}: {}", DATABASE_PATH, e.getMessage(), e);
      } finally {
        connection = null; // Ensure the reference is nullified regardless of close success
      }
    } else {
      LOGGER.debug("No active connection to close for: {}", DATABASE_PATH);
    }
  }
}
