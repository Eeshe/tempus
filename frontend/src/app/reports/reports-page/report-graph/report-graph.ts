import { formatDate } from '@angular/common';
import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  signal,
  untracked,
} from '@angular/core';
import { enumerateDays, enumerateMonths, parseYYYYMMDDDate } from '../../../shared/util/date.util';
import { durationFromMs, formatHHMMSSTime } from '../../../shared/util/time.util';
import {
  buildAxisTicks,
  ReportGraphBar,
  ReportGraphGroup,
  ReportGraphSegment,
  ReportGraphType,
  resolveAxisMaxHours,
  resolveAxisMaxMillis,
} from '../../models/report-graph.model';
import { ProjectReport, Report, ReportDay } from '../../models/report.model';

const TOOLTIP_EDGE_PADDING = 8;
const TOOLTIP_FLIP_THRESHOLD = 0.6;
const LOCALE = 'en-us';

@Component({
  selector: 'app-report-graph',
  styleUrl: './report-graph.css',
  templateUrl: './report-graph.html',
})
export class ReportGraph {
  private readonly host: ElementRef<HTMLElement> = inject(ElementRef);

  readonly report = input.required<Report<ProjectReport>>();

  readonly graphType = signal<ReportGraphType>('month');

  readonly hoveredBar = signal<ReportGraphBar | null>(null);
  readonly tooltipX = signal<number>(0);
  readonly tooltipY = signal<number>(0);
  readonly tooltipFlipped = signal<boolean>(false);

  constructor() {
    // Every new report resets the graph to the view that best fits its amount of days.
    effect(() => {
      const reportDaysCount: number = this.report().reportDays.length;
      untracked(() => this.graphType.set(this.resolveDefaultGraphType(reportDaysCount)));
    });
  }

  /**
   * <= 30 days of data renders one bar per day, 31 or more renders one bar per month.
   */
  private resolveDefaultGraphType(reportDaysCount: number): ReportGraphType {
    return reportDaysCount <= 30 ? 'month' : 'year';
  }

  readonly maxBarTrackedTimeMillis = computed<number>(() =>
    this.bars().reduce(
      (maxTrackedTimeMillis: number, bar: ReportGraphBar) =>
        Math.max(maxTrackedTimeMillis, bar.totalTrackedTimeMillis),
      0,
    ),
  );

  readonly axisMaxHours = computed<number>(() =>
    resolveAxisMaxHours(this.graphType(), this.maxBarTrackedTimeMillis()),
  );

  readonly axisMaxTrackedTimeMillis = computed<number>(() =>
    resolveAxisMaxMillis(this.graphType(), this.maxBarTrackedTimeMillis()),
  );

  readonly ticks = computed<number[]>(() => buildAxisTicks(this.graphType(), this.axisMaxHours()));

  readonly bars = computed<ReportGraphBar[]>(() => {
    const report: Report<ProjectReport> = this.report();
    return this.graphType() === 'month'
      ? this.buildMonthGraphBars(report)
      : this.buildYearGraphBars(report);
  });

  readonly groups = computed<ReportGraphGroup[]>(() => this.buildGroups(this.bars()));

  readonly axisLabel = computed<string>(() => (this.graphType() === 'month' ? 'Day' : 'Month'));

  trackBar(_index: number, bar: ReportGraphBar): string {
    return bar.key;
  }

  trackSegment(_index: number, segment: ReportGraphSegment): string {
    return segment.key;
  }

  trackGroup(_index: number, group: ReportGraphGroup): string {
    return group.key;
  }

  calculateBarHeightPercent(bar: ReportGraphBar): number {
    const axisMax: number = this.axisMaxTrackedTimeMillis();
    if (axisMax <= 0) {
      return 0;
    }
    return Math.min(100, (bar.totalTrackedTimeMillis / axisMax) * 100);
  }

  computeBarAriaLabel(bar: ReportGraphBar): string {
    const periodLabel: string =
      bar.secondaryLabel == null ? bar.primaryLabel : `${bar.secondaryLabel} ${bar.primaryLabel}`;
    return `${periodLabel}: ${this.formatDuration(bar.totalTrackedTimeMillis)}`;
  }

  showBarTooltip(bar: ReportGraphBar, event: MouseEvent): void {
    if (bar.segments.length === 0) {
      return;
    }
    this.hoveredBar.set(bar);
    this.positionTooltip(event);
  }

  updateBarTooltipPosition(event: MouseEvent): void {
    if (this.hoveredBar() == null) {
      return;
    }
    this.positionTooltip(event);
  }

  private positionTooltip(event: MouseEvent): void {
    const bounds: DOMRect = this.host.nativeElement.getBoundingClientRect();
    const x: number = event.clientX - bounds.left;
    const y: number = event.clientY - bounds.top;
    this.tooltipX.set(
      Math.min(Math.max(x, TOOLTIP_EDGE_PADDING), bounds.width - TOOLTIP_EDGE_PADDING),
    );
    this.tooltipY.set(
      Math.min(Math.max(y, TOOLTIP_EDGE_PADDING), bounds.height - TOOLTIP_EDGE_PADDING),
    );
    this.tooltipFlipped.set(x > bounds.width * TOOLTIP_FLIP_THRESHOLD);
  }

  hideBarTooltip(): void {
    this.hoveredBar.set(null);
  }

  formatHours(hours: number): string {
    return `${hours}h`;
  }

  formatDuration(trackedTimeMillis: number): string {
    return formatHHMMSSTime(durationFromMs(trackedTimeMillis));
  }

  formatPercentage(percentage: number): string {
    return `${percentage.toFixed(2)}%`;
  }

  private buildGroups(bars: ReportGraphBar[]): ReportGraphGroup[] {
    const isMonthGraph: boolean = this.graphType() === 'month';
    const groups: ReportGraphGroup[] = [];
    for (const bar of bars) {
      const key: string = isMonthGraph ? bar.key.slice(0, 7) : bar.key.slice(0, 4);
      const lastGroup: ReportGraphGroup | undefined = groups[groups.length - 1];
      if (lastGroup?.key === key) {
        lastGroup.span++;
        continue;
      }
      groups.push({ key, label: this.formatGroupLabel(key, isMonthGraph), span: 1 });
    }
    return groups;
  }

  private formatGroupLabel(key: string, isMonthGraph: boolean): string {
    if (!isMonthGraph) {
      return key;
    }
    const monthStart: Date | null = parseYYYYMMDDDate(`${key}-01`);
    return monthStart == null ? key : formatDate(monthStart, 'MMMM', LOCALE);
  }

  private buildMonthGraphBars(report: Report<ProjectReport>): ReportGraphBar[] {
    const reportDaysByDate: Map<string, ReportDay<ProjectReport>> = this.indexReportDaysByDate(
      report,
    );

    return enumerateDays(report.startDate, report.endDate).map((date: Date) => {
      const reportDay: ReportDay<ProjectReport> | undefined = reportDaysByDate.get(
        this.formatDateKey(date),
      );
      const reportEntries: ProjectReport[] = reportDay?.reportEntries ?? [];
      const totalTrackedTimeMillis: number = this.sumTrackedTimeMillis(reportEntries);

      return {
        key: this.formatDateKey(date),
        primaryLabel: String(date.getDate()).padStart(2, '0'),
        secondaryLabel: formatDate(date, 'EEE', LOCALE),
        tooltipTitle: `${formatDate(date, 'EEE, MMM d', LOCALE)} - ${this.formatDuration(totalTrackedTimeMillis)}`,
        totalTrackedTimeMillis,
        segments: this.buildSegments(reportEntries),
      };
    });
  }

  private buildYearGraphBars(report: Report<ProjectReport>): ReportGraphBar[] {
    const reportDaysByMonth: Map<string, ReportDay<ProjectReport>[]> =
      this.indexReportDaysByMonth(report);

    return enumerateMonths(report.startDate, report.endDate).map((date: Date) => {
      const monthKey: string = this.formatMonthKey(date);
      const reportEntries: ProjectReport[] = (reportDaysByMonth.get(monthKey) ?? []).flatMap(
        (reportDay: ReportDay<ProjectReport>) => reportDay.reportEntries,
      );
      const totalTrackedTimeMillis: number = this.sumTrackedTimeMillis(reportEntries);

      return {
        key: monthKey,
        primaryLabel: formatDate(date, 'MMMM', LOCALE),
        secondaryLabel: null,
        tooltipTitle: `${formatDate(date, 'MMMM, y', LOCALE)} - ${this.formatDuration(totalTrackedTimeMillis)}`,
        totalTrackedTimeMillis,
        segments: this.buildSegments(reportEntries),
      };
    });
  }

  private indexReportDaysByDate(
    report: Report<ProjectReport>,
  ): Map<string, ReportDay<ProjectReport>> {
    return new Map<string, ReportDay<ProjectReport>>(
      report.reportDays.map((reportDay: ReportDay<ProjectReport>) => [
        reportDay.date.slice(0, 10), // Ensure date is yyyy-MM-dd
        reportDay,
      ]),
    );
  }

  private indexReportDaysByMonth(
    report: Report<ProjectReport>,
  ): Map<string, ReportDay<ProjectReport>[]> {
    const reportDaysByMonth: Map<string, ReportDay<ProjectReport>[]> = new Map();
    for (const reportDay of report.reportDays) {
      const monthKey: string = reportDay.date.slice(0, 7); // Ensure it's yyyy-MM
      const reportDays: ReportDay<ProjectReport>[] = reportDaysByMonth.get(monthKey) ?? [];

      reportDays.push(reportDay);
      reportDaysByMonth.set(monthKey, reportDays);
    }

    return reportDaysByMonth;
  }

  private buildSegments(reportEntries: ProjectReport[]): ReportGraphSegment[] {
    const trackedTimeByProjectName: Map<string, { trackedTimeMillis: number; color: string }> =
      new Map();
    for (const reportEntry of reportEntries) {
      const current = trackedTimeByProjectName.get(reportEntry.project.name);
      trackedTimeByProjectName.set(reportEntry.project.name, {
        trackedTimeMillis: (current?.trackedTimeMillis ?? 0) + reportEntry.trackedTimeMillis,
        color: reportEntry.project.color,
      });
    }
    const totalTrackedTimeMillis: number = this.sumTrackedTimeMillis(reportEntries);

    return [...trackedTimeByProjectName.entries()]
      .map(([projectName, projectTrackedTime]) => ({
        key: projectName,
        label: projectName,
        color: projectTrackedTime.color,
        trackedTimeMillis: projectTrackedTime.trackedTimeMillis,
        percentage:
          totalTrackedTimeMillis === 0
            ? 0
            : (projectTrackedTime.trackedTimeMillis / totalTrackedTimeMillis) * 100,
      }))
      .sort(
        (segmentA: ReportGraphSegment, segmentB: ReportGraphSegment) =>
          segmentB.trackedTimeMillis - segmentA.trackedTimeMillis,
      );
  }

  private sumTrackedTimeMillis(reportEntries: ProjectReport[]): number {
    return reportEntries.reduce(
      (total: number, reportEntry: ProjectReport) => total + reportEntry.trackedTimeMillis,
      0,
    );
  }

  private formatDateKey(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  }

  private formatMonthKey(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
  }
}
