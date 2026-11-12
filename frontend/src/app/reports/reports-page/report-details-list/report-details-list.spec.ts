import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Project } from '../../../project/models/project.model';
import { ProjectReport, Report, ReportTimeEntry } from '../../models/report.model';
import { ReportDetailsList } from './report-details-list';

describe('ReportDetailsList', () => {
  let component: ReportDetailsList;
  let fixture: ComponentFixture<ReportDetailsList>;

  const mockReport: Report<ProjectReport> = {
    startDate: null,
    endDate: null,
    totalTrackedTimeMillis: 0,
    totalBillableTrackedTimeMillis: 0,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportDays: [],
  };

  const timeEntries: ReportTimeEntry[] = [
    createTimeEntry(1, 'FirstDescription'),
    createTimeEntry(2, 'SecondDescription'),
  ];
  const projectReport: ProjectReport = {
    project: createProject(),
    trackedTimeMillis: 7_200_000,
    timeEntries,
  };
  const reportWithEntries: Report<ProjectReport> = {
    startDate: null,
    endDate: null,
    totalTrackedTimeMillis: 7_200_000,
    totalBillableTrackedTimeMillis: 7_200_000,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportDays: [
      {
        date: '2026-01-02',
        totalTrackedTimeMillis: 7_200_000,
        totalBillableTrackedTimeMillis: 7_200_000,
        totalNonBillableTrackedTimeMillis: 0,
        totalAccumulatedPay: 0,
        reportEntries: [projectReport],
      },
    ],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDetailsList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportDetailsList);
    fixture.componentRef.setInput('report', mockReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the accumulated pay with the currency symbol', () => {
    const header: HTMLElement = fixture.nativeElement.querySelector('.total-header');

    expect(header.textContent).toContain('$0.00');
    expect(header.textContent).not.toContain('USD');
  });

  describe('project row expansion', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('report', reportWithEntries);
      await fixture.whenStable();
    });

    it('renders the time entry count of each project', () => {
      const count: HTMLElement = fixture.nativeElement.querySelector('.project-toggle__count');

      expect(count.textContent).toBe('2');
    });

    it('renders the time entries composing the project when the row is expanded', async () => {
      clickProjectRow();
      await fixture.whenStable();

      expect(fixture.nativeElement.querySelectorAll('app-report-time-entry-row').length).toBe(2);
      expect(
        fixture.nativeElement
          .querySelector('.project-toggle__chevron')
          ?.classList.contains('project-toggle__chevron--open'),
      ).toBe(true);
    });

    it('renders the newest time entry first', async () => {
      clickProjectRow();
      await fixture.whenStable();

      const element: HTMLElement = fixture.nativeElement;
      const descriptions: string[] = Array.from(
        element.querySelectorAll('.report-time-entry__description'),
      ).map((description: Element) => description.textContent?.trim() ?? '');
      expect(descriptions).toEqual(['SecondDescription', 'FirstDescription']);
    });

    it('hides the time entries when the expanded row is collapsed again', async () => {
      clickProjectRow();
      await fixture.whenStable();
      clickProjectRow();
      await fixture.whenStable();

      expect(fixture.nativeElement.querySelectorAll('app-report-time-entry-row').length).toBe(0);
    });

    function clickProjectRow(): void {
      fixture.nativeElement
        .querySelector('.sortable-table__row:not(.sortable-table__row--detail)')
        ?.click();
    }
  });

  function createProject(): Project {
    return {
      id: 1,
      name: 'MyProject',
      userId: 1,
      isPrivate: false,
      isArchived: false,
      hourlyRate: 0,
      color: '#FFFFFF',
      tasks: [],
      client: { id: 1, name: 'MyClient', userId: 1, createdAt: '2026-01-01T00:00:00Z' },
      createdAt: '2026-01-01T00:00:00Z',
    };
  }

  function createTimeEntry(id: number, description: string): ReportTimeEntry {
    return {
      id,
      description,
      isBillable: true,
      taskName: 'MyTask',
      startTime: new Date(2026, 0, 2, 9 + id, 0, 0).toISOString(),
      endTime: new Date(2026, 0, 2, 10 + id, 0, 0).toISOString(),
    };
  }
});
