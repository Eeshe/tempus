package me.eeshe.tempus.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

public class CSVTimeEntry {
    @CsvBindByName(column = "Date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate date;

    @CsvBindByName(column = "Start")
    private String start;

    @CsvBindByName(column = "End")
    private String end;

    @CsvBindByName(column = "Project")
    private String project;

    @CsvBindByName(column = "Customer")
    private String client;

    @CsvBindByName(column = "Task")
    private String task;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "Billable")
    private Boolean isBillable;

    @CsvBindByName(column = "Hourly Rate")
    private BigDecimal hourlyRate;

    @CsvBindByName(column = "Timezone")
    private String timezone;

    public CSVTimeEntry() {
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getProject() {
        return project;
    }

    public String getClient() {
        return client;
    }

    public String getTask() {
        return task;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isBillable() {
        return isBillable;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public String getTimezone() {
        return timezone;
    }
}
