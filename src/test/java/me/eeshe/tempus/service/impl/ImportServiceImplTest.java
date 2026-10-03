package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.exception.CSVTimeEntryImportException;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.model.CSVTimeEntry;
import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.model.SkippedImportEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.ImportCSVFilesRequest;
import me.eeshe.tempus.service.TimeEntryService;

@ExtendWith(MockitoExtension.class)
public class ImportServiceImplTest {
    private static final long USER_ID = 1L;

    private static final String CSV_HEADER = "Date,Start,End,Project,Customer,Task,Description,Billable,Hourly Rate";
    private static final String FILE_NAME = "time-entries.csv";
    private static final String VALID_ROW = "2026-01-01,09:00,10:00,MyProject,MyClient,MyTask,MyDescription,true,50.00";
    private static final String SECOND_VALID_ROW = "2026-01-02,09:00,10:00,MyProject,MyClient,MyTask,MyDescription,true,50.00";

    @Mock
    private TimeEntryService timeEntryService;

    @Mock
    private TimeEntryMapper timeEntryMapper;

    @InjectMocks
    private ImportServiceImpl importService;

    @Nested
    class ImportCsvFiles {

        @Test
        void importsValidRowsAndReturnsCounts() {
            final ImportCSVFilesRequest importCSVFilesRequest = new ImportCSVFilesRequest(
                    List.of(createCsvFile(FILE_NAME, VALID_ROW, SECOND_VALID_ROW)));

            when(timeEntryMapper.fromCSVTimeEntry(any(CSVTimeEntry.class), eq(USER_ID)))
                    .thenReturn(mock(CreateTimeEntryRequest.class));
            when(timeEntryService.createTimeEntries(anyList()))
                    .thenReturn(List.of(mock(TimeEntry.class), mock(TimeEntry.class)));

            final ImportResult importResult = importService.importCSVFiles(USER_ID, importCSVFilesRequest);

            assertThat(importResult.importedCount()).isEqualTo(2);
            assertThat(importResult.skippedCount()).isZero();
            assertThat(importResult.skippedEntries()).isEmpty();

            verify(timeEntryMapper, times(2)).fromCSVTimeEntry(any(CSVTimeEntry.class), eq(USER_ID));
            verify(timeEntryService).createTimeEntries(argThat(requests -> requests.size() == 2));
        }

        @Test
        void skipsUnreadableFile() throws IOException {
            final MultipartFile csvFile = mock(MultipartFile.class);

            when(csvFile.getOriginalFilename()).thenReturn("broken.csv");
            when(csvFile.getInputStream()).thenThrow(new IOException("error"));
            when(timeEntryService.createTimeEntries(anyList())).thenReturn(List.of());

            final ImportResult importResult = importService.importCSVFiles(
                    USER_ID, new ImportCSVFilesRequest(List.of(csvFile)));

            assertThat(importResult.importedCount()).isZero();
            assertThat(importResult.skippedEntries()).containsExactly(
                    new SkippedImportEntry("broken.csv", 0, null, "Could not read CSV file: error"));

            verify(timeEntryMapper, never()).fromCSVTimeEntry(any(), anyLong());
        }

        @Test
        void skipsRowWithValidationError() {
            final String csvContent = "Date,End,Project,Customer,Task,Description,Billable,Hourly Rate\n"
                    + "2026-01-01,10:00,MyProject,MyClient,MyTask,MyDescription,true,50.00";
            final ImportCSVFilesRequest importCSVFilesRequest = new ImportCSVFilesRequest(List.of(
                    new MockMultipartFile(
                            "files", FILE_NAME, "text/csv", csvContent.getBytes(StandardCharsets.UTF_8))));

            when(timeEntryService.createTimeEntries(anyList())).thenReturn(List.of());

            final ImportResult importResult = importService.importCSVFiles(USER_ID, importCSVFilesRequest);

            assertThat(importResult.importedCount()).isZero();
            assertThat(importResult.skippedEntries()).containsExactly(new SkippedImportEntry(
                    FILE_NAME, 1, LocalDate.parse("2026-01-01"), "Missing required fields: start time"));

            verify(timeEntryMapper, never()).fromCSVTimeEntry(any(), anyLong());
        }

        @Test
        void skipsRowWhenMapperThrowsImportException() {
            final ImportCSVFilesRequest importCSVFilesRequest = new ImportCSVFilesRequest(
                    List.of(createCsvFile(FILE_NAME, VALID_ROW, SECOND_VALID_ROW)));

            when(timeEntryMapper.fromCSVTimeEntry(any(CSVTimeEntry.class), eq(USER_ID)))
                    .thenReturn(mock(CreateTimeEntryRequest.class))
                    .thenThrow(new CSVTimeEntryImportException(LocalDate.parse("2026-01-02"), "MyReason"));
            when(timeEntryService.createTimeEntries(anyList())).thenReturn(List.of(mock(TimeEntry.class)));

            final ImportResult importResult = importService.importCSVFiles(USER_ID, importCSVFilesRequest);

            assertThat(importResult.importedCount()).isEqualTo(1);
            assertThat(importResult.skippedEntries()).containsExactly(new SkippedImportEntry(
                    FILE_NAME,
                    2,
                    LocalDate.parse("2026-01-02"),
                    "CSV Time entry with date 2026-01-02 couldn't be imported: MyReason"));
        }

        @Test
        void returnsZeroImportsForEmptyFileList() {
            when(timeEntryService.createTimeEntries(anyList())).thenReturn(List.of());

            final ImportResult importResult = importService.importCSVFiles(
                    USER_ID, new ImportCSVFilesRequest(List.of()));

            assertThat(importResult.importedCount()).isZero();
            assertThat(importResult.skippedEntries()).isEmpty();
        }
    }

    private static MultipartFile createCsvFile(String fileName, String... rows) {
        final StringBuilder content = new StringBuilder(CSV_HEADER);
        for (String row : rows) {
            content.append("\n").append(row);
        }

        return new MockMultipartFile(
                "files", fileName, "text/csv", content.toString().getBytes(StandardCharsets.UTF_8));
    }
}
