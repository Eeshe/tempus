import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimeEntry } from '../models/time-entry.model';
import { ActiveTimeEntry } from './active-time-entry';

describe('ActiveTimeEntry', () => {
  let component: ActiveTimeEntry;
  let fixture: ComponentFixture<ActiveTimeEntry>;

  const mockTimeEntry: TimeEntry = {
    id: 1,
    userId: 1,
    project: {
      id: 1,
      name: 'Tempus',
      userId: 1,
      isPrivate: false,
      isArchived: false,
      hourlyRate: 0,
      tasks: [],
      client: { id: 1, name: 'Tempus', userId: 1, createdAt: '2026-01-01T00:00:00Z' },
      createdAt: '2026-01-01T00:00:00Z',
    },
    task: null,
    description: 'Test description',
    isBillable: false,
    startTime: '2026-01-01T10:00:00Z',
    endTime: null,
    createdAt: '2026-01-01T10:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveTimeEntry],
    }).compileComponents();

    fixture = TestBed.createComponent(ActiveTimeEntry);
    fixture.componentRef.setInput('activeTimeEntry', mockTimeEntry);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
