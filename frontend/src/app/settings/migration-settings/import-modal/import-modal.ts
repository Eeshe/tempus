import { Component, computed, inject, signal } from '@angular/core';
import { AppModal } from '../../../shared/modal/modal';
import { ModalBase } from '../../../shared/modal/modal-base';
import { ImportResult } from '../../models/import-result.model';
import { ImportService } from '../../services/import.service';

type ImportPhase = 'select' | 'confirm' | 'importing' | 'done' | 'error';

@Component({
  imports: [AppModal],
  selector: 'app-import-modal',
  styleUrl: './import-modal.css',
  templateUrl: './import-modal.html',
})
export class ImportModal extends ModalBase {
  private readonly importService: ImportService = inject(ImportService);

  readonly phase = signal<ImportPhase>('select');
  readonly selectedFiles = signal<File[]>([]);
  readonly importResult = signal<ImportResult | null>(null);
  readonly isDragging = signal<boolean>(false);

  readonly fileCount = computed<number>(() => this.selectedFiles().length);

  readonly importConfirmationMessage = computed<string>(() => {
    const count: number = this.fileCount();
    return count === 1
      ? $localize`:Import modal|Single CSV file confirmation:You are about to import 1 CSV file`
      : $localize`:Import modal|Multiple CSV files confirmation:You are about to import ${count}:count: CSV files`;
  });

  readonly importSuccessMessage = computed<string>(() => {
    const count: number = this.importResult()?.importedCount ?? 0;
    return count === 1
      ? $localize`:Import modal|Single entry imported:Successfully imported 1 entry`
      : $localize`:Import modal|Multiple entries imported:Successfully imported ${count}:count: entries`;
  });

  readonly importErrorsTitle = computed<string>(() => {
    const count: number = this.importResult()?.skippedEntries.length ?? 0;
    return count === 1
      ? $localize`:Import modal|Single import error:There was the following import error`
      : $localize`:Import modal|Multiple import errors:There were the following import errors (${count}:count:)`;
  });

  addSelectedFiles(event: Event): void {
    const input: HTMLInputElement = event.target as HTMLInputElement;
    this.acceptFiles(Array.from(input.files ?? []));

    input.value = '';
  }

  enableDragging(event: DragEvent): void {
    event.preventDefault();

    this.isDragging.set(true);
  }

  disableDragging(): void {
    this.isDragging.set(false);
  }

  addDraggedFiles(event: DragEvent): void {
    event.preventDefault();

    this.isDragging.set(false);
    this.acceptFiles(Array.from(event.dataTransfer?.files ?? []));
  }

  confirmImport(): void {
    this.phase.set('importing');
    this.importService.importCSVFiles(this.selectedFiles()).subscribe({
      next: (result: ImportResult) => {
        this.importResult.set(result);
        this.phase.set('done');
      },
      error: () => {
        this.phase.set('error');
      },
    });
  }

  override close(): void {
    if (this.phase() === 'importing') {
      return;
    }
    super.close();
  }

  private acceptFiles(files: File[]): void {
    const csvFiles: File[] = files.filter((file) => file.name.toLowerCase().endsWith('.csv'));
    if (csvFiles.length === 0) {
      return;
    }
    this.selectedFiles.set(csvFiles);
    this.phase.set('confirm');
  }
}
