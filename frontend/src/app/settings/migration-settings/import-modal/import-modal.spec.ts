import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ImportResult } from '../../models/import-result.model';
import { ImportService } from '../../services/import.service';
import { ImportModal } from './import-modal';

describe('ImportModal', () => {
  let component: ImportModal;
  let fixture: ComponentFixture<ImportModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportModal],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('moves to confirm with only the CSV files selected', () => {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('.dropzone__input');
    const csv: File = new File(['a'], 'data.csv', { type: 'text/csv' });
    const txt: File = new File(['b'], 'notes.txt', { type: 'text/plain' });
    Object.defineProperty(input, 'files', { value: [csv, txt] });

    component.onFilesSelected({ target: input } as unknown as Event);
    fixture.detectChanges();

    expect(component.phase()).toBe('confirm');
    expect(component.fileCount()).toBe(1);
  });

  it('ignores a drop that contains no CSV files', () => {
    const txt: File = new File(['b'], 'notes.txt', { type: 'text/plain' });
    const event = {
      preventDefault: () => {},
      dataTransfer: { files: [txt] },
    } as unknown as DragEvent;

    component.onDrop(event);
    fixture.detectChanges();

    expect(component.phase()).toBe('select');
    expect(component.fileCount()).toBe(0);
  });

  it('imports the files and shows the result', () => {
    const importService: ImportService = TestBed.inject(ImportService);
    const result: ImportResult = {
      importedCount: 3,
      skippedCount: 1,
      skippedEntries: [
        { fileName: 'data.csv', rowNumber: 2, date: '2026-01-01', reason: 'Invalid date' },
      ],
    };
    vi.spyOn(importService, 'importCSVFiles').mockReturnValue(of(result));

    component.selectedFiles.set([new File(['a'], 'data.csv')]);
    component.confirmImport();
    fixture.detectChanges();

    expect(component.phase()).toBe('done');
    expect(component.importResult()).toEqual(result);
    expect(fixture.nativeElement.querySelector('.message--success').textContent).toContain(
      'Successfully imported 3 entries',
    );
    expect(fixture.nativeElement.querySelector('.errors__title')).toBeTruthy();
  });

  it('shows an error when the request fails', () => {
    const importService: ImportService = TestBed.inject(ImportService);
    vi.spyOn(importService, 'importCSVFiles').mockReturnValue(throwError(() => new Error('boom')));

    component.selectedFiles.set([new File(['a'], 'data.csv')]);
    component.confirmImport();
    fixture.detectChanges();

    expect(component.phase()).toBe('error');
    expect(fixture.nativeElement.querySelector('.message--error')).toBeTruthy();
  });

  it('does not close while importing', () => {
    component.phase.set('importing');
    const closeSpy = vi.fn();
    component.closeEvent.subscribe(closeSpy);

    component.close();

    expect(closeSpy).not.toHaveBeenCalled();
  });

  it('emits close when not importing', () => {
    const closeSpy = vi.fn();
    component.closeEvent.subscribe(closeSpy);

    component.close();

    expect(closeSpy).toHaveBeenCalled();
  });
});
