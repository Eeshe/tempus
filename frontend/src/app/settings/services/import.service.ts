import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { ImportResult } from '../models/import-result.model';

@Service()
export class ImportService {
  private readonly url: string = '/api/v1/import';
  private http: HttpClient = inject(HttpClient);

  importCSVFiles(csvFiles: File[]): Observable<ImportResult> {
    const formData: FormData = new FormData();
    csvFiles.forEach((csvFile) => formData.append('files', csvFile, csvFile.name));
    return this.http.post<ImportResult>(this.url, formData, { withCredentials: true });
  }
}
