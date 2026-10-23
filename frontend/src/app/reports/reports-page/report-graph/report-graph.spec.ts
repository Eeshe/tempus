import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Client } from '../../../client/models/client.model';
import { Project } from '../../../project/models/project.model';
import { ReportGraphBar } from '../../models/report-graph.model';
import { ProjectReport, Report, ReportDay } from '../../models/report.model';
import { ReportGraph } from './report-graph';

describe('ReportGraph', () => {
  let component: ReportGraph;
  let fixture: ComponentFixture<ReportGraph>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportGraph],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportGraph);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('report', createReport([], '2026-08-01', '2026-08-03'));
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  async function setReport(report: Report<ProjectReport>): Promise<void> {
    fixture.componentRef.setInput('report', report);
    await fixture.whenStable();
  }

  it('defaults to the month graph for small reports', () => {
    expect(component.graphType()).toBe('month');
  });

  it('defaults to the year graph when the report has 31 days or more', async () => {
    await setReport(createReportWithDays(31));

    expect(component.graphType()).toBe('year');
  });

  it('resets the manually selected graph type when a new report arrives', async () => {
    component.graphType.set('year');
    await setReport(createReportWithDays(2));

    expect(component.graphType()).toBe('month');
  });

  it('derives the axis max from the tallest bar of the range', async () => {
    await setReport(
      createReport(
        [
          createReportDay('2026-08-02', [
            createProjectReport(createProject('ProjectA'), 7 * 3_600_000),
          ]),
        ],
        '2026-08-01',
        '2026-08-03',
      ),
    );

    expect(component.axisMaxHours()).toBe(8);
    expect(component.axisMaxTrackedTimeMillis()).toBe(8 * 3_600_000);
    expect(component.calculateBarHeightPercent(component.bars()[1])).toBe(87.5);
  });

  it('does not add an extra step when the tallest bar sits on a step', async () => {
    await setReport(
      createReport(
        [
          createReportDay('2026-08-02', [
            createProjectReport(createProject('ProjectA'), 8 * 3_600_000),
          ]),
        ],
        '2026-08-01',
        '2026-08-03',
      ),
    );

    expect(component.axisMaxHours()).toBe(8);
    expect(component.calculateBarHeightPercent(component.bars()[1])).toBe(100);
  });

  it('keeps a single step axis when the range has no tracked time', () => {
    expect(component.axisMaxHours()).toBe(4);
    expect(component.ticks()).toEqual([0, 4]);
  });

  it('derives the year axis max from the tallest month bar', async () => {
    await setReport(
      createReport(
        [
          createReportDay('2026-07-02', [
            createProjectReport(createProject('ProjectA'), 40 * 3_600_000),
          ]),
        ],
        '2026-07-01',
        '2026-09-01',
      ),
    );
    component.graphType.set('year');

    expect(component.axisMaxHours()).toBe(40);
    expect(component.ticks()).toEqual([0, 40]);
  });

  it('renders one bar per day and one Y label per tick', async () => {
    const element: HTMLElement = fixture.nativeElement;
    await fixture.whenStable();

    expect(element.querySelectorAll('.graph__column')).toHaveLength(3);
    expect(element.querySelectorAll('.graph__y-label')).toHaveLength(2);
    expect(element.querySelectorAll('.graph__gridline')).toHaveLength(2);
  });

  it('formats hours, durations and percentages', () => {
    expect(component.formatHours(4)).toBe('4h');
    expect(component.formatDuration(3_600_000)).toBe('01:00:00');
    expect(component.formatPercentage(33.333)).toBe('33.33%');
  });

  describe('month bars', () => {
    it('renders one bar per day of the range, including empty days', async () => {
      await setReport(
        createReport(
          [
            createReportDay('2026-08-20', [
              createProjectReport(createProject('ProjectA'), 1_800_000),
            ]),
          ],
          '2026-08-19',
          '2026-08-21',
        ),
      );

      const bars: ReportGraphBar[] = component.bars();

      expect(bars.map((bar) => bar.key)).toEqual(['2026-08-19', '2026-08-20', '2026-08-21']);
      expect(bars[0].segments).toEqual([]);
      expect(bars[0].totalTrackedTimeMillis).toBe(0);
      expect(bars[1].totalTrackedTimeMillis).toBe(1_800_000);
      expect(bars[1].primaryLabel).toBe('20');
      expect(bars[1].secondaryLabel).toBeTruthy();
      expect(bars[1].tooltipTitle.endsWith('00:30:00')).toBe(true);
    });

    it('merges entries of the same project and computes percentages', async () => {
      const projectA: Project = createProject('ProjectA');
      const projectB: Project = createProject('ProjectB');
      await setReport(
        createReport(
          [
            createReportDay('2026-08-20', [
              createProjectReport(projectA, 1_800_000),
              createProjectReport(projectB, 1_800_000),
              createProjectReport(projectA, 1_800_000),
            ]),
          ],
          '2026-08-20',
          '2026-08-20',
        ),
      );

      const segments = component.bars()[0].segments;

      expect(segments.map((segment) => segment.label)).toEqual(['ProjectA', 'ProjectB']);
      expect(segments.map((segment) => segment.trackedTimeMillis)).toEqual([3_600_000, 1_800_000]);
      expect(segments.map((segment) => segment.percentage)).toEqual([(2 / 3) * 100, (1 / 3) * 100]);
    });

    it('returns no bars when the report has no date range', async () => {
      await setReport(createReport([], null, null));

      expect(component.bars()).toEqual([]);
    });
  });

  describe('year bars', () => {
    it('renders one bar per month of the range, aggregating days', async () => {
      const project: Project = createProject('ProjectA');
      await setReport(
        createReport(
          [
            createReportDay('2026-07-01', [createProjectReport(project, 1_800_000)]),
            createReportDay('2026-07-31', [createProjectReport(project, 1_800_000)]),
            createReportDay('2026-09-05', [createProjectReport(project, 3_600_000)]),
          ],
          '2026-07-15',
          '2026-09-01',
        ),
      );
      component.graphType.set('year');

      const bars: ReportGraphBar[] = component.bars();

      expect(bars.map((bar) => bar.key)).toEqual(['2026-07', '2026-08', '2026-09']);
      expect(bars[0].totalTrackedTimeMillis).toBe(3_600_000);
      expect(bars[0].segments).toHaveLength(1);
      expect(bars[1].segments).toEqual([]);
      expect(bars[2].totalTrackedTimeMillis).toBe(3_600_000);
      expect(bars[0].secondaryLabel).toBeNull();
    });

    it('spans year boundaries', async () => {
      await setReport(createReport([], '2025-11-10', '2026-02-05'));
      component.graphType.set('year');

      expect(component.bars().map((bar) => bar.key)).toEqual([
        '2025-11',
        '2025-12',
        '2026-01',
        '2026-02',
      ]);
    });
  });

  describe('axis groups', () => {
    it('groups the month graph bars by month with a full month name', async () => {
      await setReport(createReport([], '2026-10-30', '2026-11-02'));

      expect(component.groups()).toEqual([
        { key: '2026-10', label: 'October', span: 2 },
        { key: '2026-11', label: 'November', span: 2 },
      ]);
    });

    it('renders a single full width group for a one month range', async () => {
      await setReport(createReport([], '2026-08-01', '2026-08-03'));

      expect(component.groups()).toEqual([{ key: '2026-08', label: 'August', span: 3 }]);
    });

    it('groups the year graph bars by year', async () => {
      await setReport(createReport([], '2025-11-10', '2026-02-05'));
      component.graphType.set('year');

      expect(component.groups()).toEqual([
        { key: '2025', label: '2025', span: 2 },
        { key: '2026', label: '2026', span: 2 },
      ]);
    });

    it('returns no groups when the report has no date range', async () => {
      await setReport(createReport([], null, null));

      expect(component.groups()).toEqual([]);
    });

    it('renders one group element per axis group', async () => {
      await setReport(createReport([], '2026-10-30', '2026-11-02'));
      const element: HTMLElement = fixture.nativeElement;
      await fixture.whenStable();

      const groupLabels: string[] = Array.from(element.querySelectorAll('.graph__group')).map(
        (group: Element) => group.textContent?.trim() ?? '',
      );
      expect(groupLabels).toEqual(['October', 'November']);
    });
  });
});

function createReportWithDays(dayCount: number): Report<ProjectReport> {
  const reportDays: ReportDay<ProjectReport>[] = [];
  for (let day = 1; day <= dayCount; day++) {
    reportDays.push(
      createReportDay(`2026-01-${String(day).padStart(2, '0')}`, [
        createProjectReport(createProject('ProjectA'), 3_600_000),
      ]),
    );
  }
  return createReport(reportDays, '2026-01-01', `2026-01-${String(dayCount).padStart(2, '0')}`);
}

function createProject(name: string): Project {
  return {
    id: 1,
    name,
    userId: 1,
    isPrivate: false,
    isArchived: false,
    hourlyRate: 0,
    tasks: [],
    client: createClient(),
    createdAt: '2026-01-01T00:00:00Z',
  };
}

function createClient(): Client {
  return {
    id: 1,
    name: 'MyClient',
    userId: 1,
    createdAt: '2026-01-01T00:00:00Z',
  };
}

function createProjectReport(project: Project, trackedTimeMillis: number): ProjectReport {
  return { project, trackedTimeMillis };
}

function createReportDay(date: string, reportEntries: ProjectReport[]): ReportDay<ProjectReport> {
  const totalTrackedTimeMillis: number = reportEntries.reduce(
    (total, entry) => total + entry.trackedTimeMillis,
    0,
  );
  return {
    date,
    totalTrackedTimeMillis,
    totalBillableTrackedTimeMillis: 0,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportEntries,
  };
}

function createReport(
  reportDays: ReportDay<ProjectReport>[],
  startDate: string | null,
  endDate: string | null,
): Report<ProjectReport> {
  return {
    startDate,
    endDate,
    totalTrackedTimeMillis: 0,
    totalBillableTrackedTimeMillis: 0,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportDays,
  };
}
