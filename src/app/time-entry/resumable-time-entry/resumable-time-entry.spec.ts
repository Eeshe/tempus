import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimeEntry } from '../models/time-entry.model';
import { ResumableTimeEntry } from './resumable-time-entry';

describe('ResumableTimeEntry', () => {
  let component: ResumableTimeEntry;
  let fixture: ComponentFixture<ResumableTimeEntry>;

  const mockTimeEntry: TimeEntry = {
    id: 1,
    userId: 1,
    project: { id: 1, name: 'Tempus', userId: 1, isPrivate: false, isArchived: false, hourlyRate: 0, tasks: [], clientId: 1, createdAt: '2026-01-01T00:00:00Z' },
    task: null,
    description: 'Test description',
    isBillable: false,
    startTime: '2026-01-01T10:00:00Z',
    endTime: '2026-01-01T11:00:00Z',
    createdAt: '2026-01-01T10:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResumableTimeEntry],
    }).compileComponents();

    fixture = TestBed.createComponent(ResumableTimeEntry);
    fixture.componentRef.setInput('timeEntry', mockTimeEntry);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
