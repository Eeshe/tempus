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
