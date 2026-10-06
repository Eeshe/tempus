import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimeEntry } from '../models/time-entry.model';
import { createEmptyTimeEntryPage } from '../models/time-entry-page.model';
import { TimeEntryStore } from '../stores/time-entry.store';
import { TimeEntryList } from './time-entry-list';

describe('TimeEntryList', () => {
  let component: TimeEntryList;
  let fixture: ComponentFixture<TimeEntryList>;
  let store: TimeEntryStore;
  let httpMock: HttpTestingController;

  const mockTimeEntry: TimeEntry = {
    id: 1,
    userId: 1,
    project: { id: 1, name: 'Tempus', userId: 1, isPrivate: false, isArchived: false, hourlyRate: 0, tasks: [], client: { id: 1, name: 'Client', userId: 1, createdAt: '2026-01-01T00:00:00Z' }, createdAt: '2026-01-01T00:00:00Z' },
    task: null,
    description: 'Test description',
    isBillable: false,
    startTime: '2026-01-01T10:00:00Z',
    endTime: null,
    createdAt: '2026-01-01T10:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeEntryList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(TimeEntryStore);

    fixture = TestBed.createComponent(TimeEntryList);
    component = fixture.componentInstance;
    await fixture.whenStable();

    httpMock.expectOne((request) => request.method === 'GET').flush(createEmptyTimeEntryPage());
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('remembers the last stopped time entry', async () => {
    store.stopActive(mockTimeEntry);

    httpMock.expectOne((request) => request.method === 'PATCH').flush(mockTimeEntry);
    await fixture.whenStable();

    expect(store.lastStoppedTimeEntry()).toEqual(mockTimeEntry);
  });

  it('resumes the last stopped time entry on R and clears the memory', async () => {
    store.stopActive(mockTimeEntry);
    httpMock.expectOne((request) => request.method === 'PATCH').flush(mockTimeEntry);
    await fixture.whenStable();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'r' }));
    await fixture.whenStable();

    const resumeRequest = httpMock.expectOne((request) => request.method === 'POST');
    expect(resumeRequest.request.body.projectId).toBe(mockTimeEntry.project.id);
    resumeRequest.flush({ ...mockTimeEntry, id: 2 });

    httpMock.expectOne((request) => request.method === 'GET').flush(createEmptyTimeEntryPage());
    await fixture.whenStable();

    expect(store.lastStoppedTimeEntry()).toBeNull();
  });

  it('does nothing on R when no time entry was stopped', async () => {
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'r' }));
    await fixture.whenStable();

    httpMock.expectNone((request) => request.method === 'POST');
    expect(store.lastStoppedTimeEntry()).toBeNull();
  });

  it('does not resume on R while an input is focused', async () => {
    store.stopActive(mockTimeEntry);
    httpMock.expectOne((request) => request.method === 'PATCH').flush(mockTimeEntry);
    await fixture.whenStable();

    const input: HTMLInputElement = document.createElement('input');
    document.body.appendChild(input);
    input.focus();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'r' }));
    await fixture.whenStable();

    httpMock.expectNone((request) => request.method === 'POST');
    expect(store.lastStoppedTimeEntry()).toEqual(mockTimeEntry);

    input.remove();
  });
});
