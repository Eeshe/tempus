import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Project } from '../../project/models/project.model';
import { TimeEntryPage } from '../models/time-entry-page.model';
import { TimeEntry } from '../models/time-entry.model';
import { TimeEntryStore } from './time-entry.store';

describe('TimeEntryStore', () => {
  let store: TimeEntryStore;
  let httpMock: HttpTestingController;

  const project: Project = {
    id: 1,
    name: 'Tempus',
    userId: 1,
    isPrivate: false,
    isArchived: false,
    hourlyRate: 0,
    tasks: [],
    client: { id: 1, name: 'Client', userId: 1, createdAt: '2026-01-01T00:00:00Z' },
    createdAt: '2026-01-01T00:00:00Z',
  };

  function createTimeEntry(overrides: Partial<TimeEntry> & Pick<TimeEntry, 'id'>): TimeEntry {
    return {
      userId: 1,
      project: project,
      task: null,
      description: 'Test description',
      isBillable: false,
      startTime: '2026-01-01T10:00:00Z',
      endTime: '2026-01-01T11:00:00Z',
      createdAt: '2026-01-01T10:00:00Z',
      ...overrides,
    };
  }

  function createPage(content: TimeEntry[]): TimeEntryPage {
    return {
      content: content,
      currentCursor: '',
      previousCursor: null,
      nextCursor: null,
      page: 0,
      size: content.length,
      totalElements: content.length,
      totalPages: 1,
      first: true,
      last: true,
    };
  }

  function load(content: TimeEntry[]): void {
    store.loadPage().subscribe();
    httpMock.expectOne((request) => request.method === 'GET').flush(createPage(content));
  }

  function loadAtCursor(cursor: string, content: TimeEntry[]): void {
    store.loadPage().subscribe();
    httpMock.expectOne((request) => request.method === 'GET').flush({ ...createPage(content), currentCursor: cursor });
  }

  function groupIds(): string[] {
    return Array.from(store.dayGroupedTimeEntries()[0].allEntries.values()).map((group) => group.id);
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(TimeEntryStore);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('reuses the same group id when only entry times change', () => {
    load([createTimeEntry({ id: 1 }), createTimeEntry({ id: 2 })]);
    const idBefore: string = groupIds()[0];

    load([
      createTimeEntry({ id: 1, endTime: '2026-01-01T11:30:00Z' }),
      createTimeEntry({ id: 2 }),
    ]);

    expect(groupIds()).toEqual([idBefore]);
  });

  it('keeps the group id when the oldest entry is deleted', () => {
    load([createTimeEntry({ id: 1 }), createTimeEntry({ id: 2 })]);
    const idBefore: string = groupIds()[0];

    load([createTimeEntry({ id: 2 })]);

    expect(groupIds()).toEqual([idBefore]);
  });

  it('keeps the group id across a bulk description edit', () => {
    load([createTimeEntry({ id: 1 }), createTimeEntry({ id: 2 })]);
    const idBefore: string = groupIds()[0];

    load([
      createTimeEntry({ id: 1, description: 'Renamed' }),
      createTimeEntry({ id: 2, description: 'Renamed' }),
    ]);

    expect(groupIds()).toEqual([idBefore]);
  });

  it('keeps the id of a group that absorbs an entry from another group', () => {
    const otherProject: Project = { ...project, id: 2, name: 'Other' };
    load([
      createTimeEntry({ id: 1 }),
      createTimeEntry({ id: 2, project: otherProject }),
    ]);
    const idsBefore: string[] = groupIds();

    // Entry 1 changes project, so it joins entry 2's group. That group keeps its own id.
    load([
      createTimeEntry({ id: 1, project: otherProject }),
      createTimeEntry({ id: 2, project: otherProject }),
    ]);

    const idsAfter: string[] = groupIds();
    expect(idsAfter).toHaveLength(1);
    expect(idsBefore).toContain(idsAfter[0]);
  });

  it('scopes the day id by page cursor so a day reused across pages is not reused in the DOM', () => {
    const content: TimeEntry[] = [createTimeEntry({ id: 1 })];

    loadAtCursor('2026-01-01T00:00:00.000Z', content);
    const firstDay = store.dayGroupedTimeEntries()[0];

    // Same entries, same local dayKey, different page cursor (e.g. the boundary day shared by two pages).
    loadAtCursor('2025-12-31T00:00:00.000Z', content);
    const secondDay = store.dayGroupedTimeEntries()[0];

    expect(secondDay.dayKey).toBe(firstDay.dayKey);
    expect(secondDay.id).not.toBe(firstDay.id);
  });
});
