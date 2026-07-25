package me.eeshe.tempus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class ControllerTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("PRAGMA foreign_keys = OFF");
        jdbcTemplate.execute("DELETE FROM time_entries");
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM projects");
        jdbcTemplate.execute("DELETE FROM clients");
        jdbcTemplate.execute("DELETE FROM group_users");
        jdbcTemplate.execute("DELETE FROM groups");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");
    }
}