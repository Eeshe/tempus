export interface SkippedImportEntry {
  fileName: string;
  rowNumber: number;
  date: string;
  reason: string;
}

export interface ImportResult {
  importedCount: number;
  skippedCount: number;
  skippedEntries: SkippedImportEntry[];
}
