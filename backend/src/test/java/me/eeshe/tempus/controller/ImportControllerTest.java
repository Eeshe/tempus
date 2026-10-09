package me.eeshe.tempus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.servlet.ServletException;
import me.eeshe.tempus.controller.ImportController;
import me.eeshe.tempus.dto.ImportCSVFilesRequestDTO;
import me.eeshe.tempus.dto.ImportResultDTO;
import me.eeshe.tempus.dto.SkippedImportEntryDTO;
import me.eeshe.tempus.exception.CSVFileReadException;
import me.eeshe.tempus.mapper.ImportMapper;
import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.model.SkippedImportEntry;
import me.eeshe.tempus.request.ImportCSVFilesRequest;
import me.eeshe.tempus.service.ImportService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(ImportController.class)
public class ImportControllerTest extends ControllerTestBase {
    private static final String CSV_FILE_NAME = "time-entries.csv";
    private static final String NON_CSV_FILE_NAME = "notes.txt";
    private static final LocalDate SKIPPED_ENTRY_DATE = LocalDate.parse("2026-01-02");

    @MockitoBean
    private ImportService importService;

    @MockitoBean
    private ImportMapper importMapper;

    @Nested
    class ImportCsvFiles {
        private static final String URL = "/api/v1/import";

        @Test
        void importsCsvFiles() {
            final ImportCSVFilesRequest importCSVFilesRequest = createImportCSVFilesRequest();
            final ImportResult importResult = createImportResult();
            final ImportResultDTO importResultDTO = createImportResultDTO();

            when(importMapper.fromDTO(any(ImportCSVFilesRequestDTO.class))).thenReturn(importCSVFilesRequest);
            when(importService.importCSVFiles(USER_ID, importCSVFilesRequest)).thenReturn(importResult);
            when(importMapper.toDTO(importResult)).thenReturn(importResultDTO);

            assertThat(mockMvc.post().multipart().uri(URL)
                    .file(createCsvFile())
                    .with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createImportResultDTOJson());

            final ArgumentCaptor<ImportCSVFilesRequestDTO> requestDTOCaptor = ArgumentCaptor
                    .forClass(ImportCSVFilesRequestDTO.class);
            verify(importMapper).fromDTO(requestDTOCaptor.capture());
            assertThat(requestDTOCaptor.getValue().files()).hasSize(1);
            assertThat(requestDTOCaptor.getValue().files().get(0).getOriginalFilename()).isEqualTo(CSV_FILE_NAME);

            verify(importService).importCSVFiles(USER_ID, importCSVFilesRequest);
            verify(importMapper).toDTO(importResult);
        }

        @Test
        void importsMultipleCsvFiles() {
            final ImportCSVFilesRequest importCSVFilesRequest = new ImportCSVFilesRequest(
                    List.of(createCsvFile(), createSecondCsvFile()));
            final ImportResult importResult = createImportResult();
            final ImportResultDTO importResultDTO = createImportResultDTO();

            when(importMapper.fromDTO(any(ImportCSVFilesRequestDTO.class))).thenReturn(importCSVFilesRequest);
            when(importService.importCSVFiles(USER_ID, importCSVFilesRequest)).thenReturn(importResult);
            when(importMapper.toDTO(importResult)).thenReturn(importResultDTO);

            assertThat(mockMvc.post().multipart().uri(URL)
                    .file(createCsvFile())
                    .file(createSecondCsvFile())
                    .with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createImportResultDTOJson());

            final ArgumentCaptor<ImportCSVFilesRequestDTO> requestDTOCaptor = ArgumentCaptor
                    .forClass(ImportCSVFilesRequestDTO.class);
            verify(importMapper).fromDTO(requestDTOCaptor.capture());
            assertThat(requestDTOCaptor.getValue().files()).hasSize(2);

            verify(importService).importCSVFiles(USER_ID, importCSVFilesRequest);
        }

        @Test
        void rejectsUnreadableCsvFile() {
            final ImportCSVFilesRequest importCSVFilesRequest = createImportCSVFilesRequest();

            when(importMapper.fromDTO(any(ImportCSVFilesRequestDTO.class))).thenReturn(importCSVFilesRequest);
            when(importService.importCSVFiles(USER_ID, importCSVFilesRequest))
                    .thenThrow(new CSVFileReadException("Could not read CSV file", new RuntimeException("boom")));

            assertThat(mockMvc.post().multipart().uri(URL)
                    .file(createCsvFile())
                    .with(createPrincipalWithCsrf(USER_ID)))
                    .failure()
                    .isInstanceOf(ServletException.class)
                    .hasCauseInstanceOf(CSVFileReadException.class)
                    .hasMessageContaining("Could not read CSV file");

            verify(importService).importCSVFiles(USER_ID, importCSVFilesRequest);
            verify(importMapper, never()).toDTO(any(ImportResult.class));
        }

        @Test
        void rejectsMissingFiles() {
            assertThat(mockMvc.post().multipart().uri(URL).with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(ImportCSVFilesRequestDTO.ERROR_MESSAGE_NO_FILES);

            verifyNoInteractions(importMapper, importService);
        }

        @Test
        void rejectsNonCsvFile() {
            assertThat(mockMvc.post().multipart().uri(URL)
                    .file(createNonCsvFile())
                    .with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(ImportCSVFilesRequestDTO.ERROR_MESSAGE_NOT_CSV);

            verifyNoInteractions(importMapper, importService);
        }

        @Test
        void rejectsWrongFieldName() {
            assertThat(mockMvc.post().multipart().uri(URL)
                    .file(createWrongFieldNameFile())
                    .with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(ImportCSVFilesRequestDTO.ERROR_MESSAGE_NO_FILES);

            verifyNoInteractions(importMapper, importService);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().multipart().uri(URL).with(createCsrf())
                    .file(createCsvFile()))
                    .hasStatus(401);

            verifyNoInteractions(importMapper, importService);
        }
    }

    private static MockMultipartFile createCsvFile() {
        return new MockMultipartFile("files", CSV_FILE_NAME, "text/csv", "name,start,end".getBytes());
    }

    private static MockMultipartFile createSecondCsvFile() {
        return new MockMultipartFile("files", "time-entries-2.csv", "text/csv", "name,start,end".getBytes());
    }

    private static MockMultipartFile createWrongFieldNameFile() {
        return new MockMultipartFile("wrong", CSV_FILE_NAME, "text/csv", "name,start,end".getBytes());
    }

    private static MockMultipartFile createNonCsvFile() {
        return new MockMultipartFile("files", NON_CSV_FILE_NAME, "text/plain", "not a csv".getBytes());
    }

    private static ImportCSVFilesRequest createImportCSVFilesRequest() {
        return new ImportCSVFilesRequest(List.of(createCsvFile()));
    }

    private static ImportResult createImportResult() {
        return new ImportResult(2L, List.of(createSkippedImportEntry()));
    }

    private static SkippedImportEntry createSkippedImportEntry() {
        return new SkippedImportEntry(CSV_FILE_NAME, 3, SKIPPED_ENTRY_DATE, "MyReason");
    }

    private static ImportResultDTO createImportResultDTO() {
        return new ImportResultDTO(2L, 1L, List.of(createSkippedImportEntryDTO()));
    }

    private static SkippedImportEntryDTO createSkippedImportEntryDTO() {
        return new SkippedImportEntryDTO(CSV_FILE_NAME, 3, SKIPPED_ENTRY_DATE, "MyReason");
    }

    private static String createImportResultDTOJson() {
        return """
                {
                    "importedCount": 2,
                    "skippedCount": 1,
                    "skippedEntries": [%s]
                }""".formatted(createSkippedImportEntryDTOJson());
    }

    private static String createSkippedImportEntryDTOJson() {
        return """
                {
                    "fileName": "%s",
                    "rowNumber": 3,
                    "date": "%s",
                    "reason": "MyReason"
                }""".formatted(CSV_FILE_NAME, SKIPPED_ENTRY_DATE);
    }
}
