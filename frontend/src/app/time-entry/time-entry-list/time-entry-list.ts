import { AsyncPipe, formatDate } from '@angular/common';
import { afterNextRender, Component, computed, HostListener, inject, Injector, signal, Signal } from '@angular/core';
import { map, Subscription } from 'rxjs';
import { PageNavigator } from '../../shared/pagination/page-navigator/page-navigator';
import { PagedListBase } from '../../shared/pagination/paged-list-base';
import { TimerService } from '../../shared/services/timer.service';
import { computeDuration, durationFromMs, formatHHMMSSTime } from '../../shared/util/time.util';
import { ActiveTimeEntry } from '../active-time-entry/active-time-entry';
import { TimeEntryPage } from '../models/time-entry-page.model';
import { TimeEntry } from '../models/time-entry.model';
import { ResumableTimeEntryGroup } from '../resumable-time-entry-group/resumable-time-entry-group';
import { DayGroupedTimeEntries, TimeEntryStore } from '../stores/time-entry.store';

@Component({
  imports: [ActiveTimeEntry, PageNavigator, ResumableTimeEntryGroup, AsyncPipe],
  selector: 'app-time-entry-list',
  styleUrl: './time-entry-list.css',
  templateUrl: './time-entry-list.html',
})
export class TimeEntryList extends PagedListBase {
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);
  private readonly timerService: TimerService = inject(TimerService);
  private readonly injector: Injector = inject(Injector);

  readonly timeEntryPage: Signal<TimeEntryPage> = this.timeEntryStore.timeEntryPage;
  readonly activeTimeEntries: Signal<TimeEntry[]> = this.timeEntryStore.activeTimeEntries;
  readonly dayGroupedTimeEntries: Signal<DayGroupedTimeEntries[]> = this.timeEntryStore.dayGroupedTimeEntries;

  // True until the first page has rendered. Enter/leave animations are suppressed during that
  // render so the initial load does not animate every entry in.
  private readonly isInitialLoad = signal<boolean>(true);

  // Identifies the most recent page request. A stale response may only clear the page-changing
  // flag if it still belongs to the latest request.
  private pageChangeRequestId: number = 0;
  private pageChangeSubscription: Subscription | null = null;

  readonly enterClass = computed(() => this.isInitialLoad() ? '' : this.isPageChanging() ? 'animate-page-switch-in' : 'animate-expand');
  readonly leaveClass = computed(() => this.isInitialLoad() ? '' : this.isPageChanging() ? 'animate-page-switch-out' : 'animate-collapse');

  readonly todayFormattedTime$ = this.timerService.oneSecondTick$.pipe(map(() => {
    const allTodayTimeEntries: TimeEntry[] = Array.from(this.dayGroupedTimeEntries()[0].allEntries.values()).flatMap(timeEntryGroup => timeEntryGroup.entries);
    const totalTrackedTimeMs: number = allTodayTimeEntries.reduce((sum, timeEntry) => {
      const trackedTimeMs: number = computeDuration(timeEntry.startTime, timeEntry.endTime!)!.totalMilliseconds;

      return sum + trackedTimeMs;
    }, 0);

    return formatHHMMSSTime(durationFromMs(totalTrackedTimeMs));
  }));

  constructor() {
    super();

    this.timeEntryStore.loadPage().subscribe({
      next: () => this.endInitialLoadAfterRender(),
      error: () => this.isInitialLoad.set(false),
    });
  }

  override increasePage(): void {
    this.changePage(this.timeEntryPage().nextCursor);
  }

  override decreasePage(): void {
    this.changePage(this.timeEntryPage().previousCursor);
  }

  private changePage(cursor: string | null): void {
    const requestId: number = ++this.pageChangeRequestId;

    this.startPageChanging();

    // Cancel any in-flight page request so a slow earlier response cannot apply a stale page.
    this.pageChangeSubscription?.unsubscribe();
    this.pageChangeSubscription = this.timeEntryStore.loadPage(cursor).subscribe({
      next: () => this.endPageChangingAfterRender(requestId),
      error: () => {
        if (requestId === this.pageChangeRequestId) {
          this.endPageChanging();
        }
      },
    });
  }

  private endInitialLoadAfterRender(): void {
    afterNextRender(() => this.isInitialLoad.set(false), { injector: this.injector });
  }

  private endPageChangingAfterRender(requestId: number): void {
    // Wait for the render that inserts the new page so the entering elements capture the
    // page-switch animation classes; ignore if a newer page change has started in the meantime.
    afterNextRender(() => {
      if (requestId === this.pageChangeRequestId) {
        this.endPageChanging();
      }
    }, { injector: this.injector });
  }

  countTotalTimeEntries(map: Map<string, TimeEntry[]>): number {
    return [...map.values()].reduce((sum, arr) => sum + arr.length, 0);
  }

  isTodayGroup(dayGroupedTimeEntries: DayGroupedTimeEntries): boolean {
    return dayGroupedTimeEntries.dayKey === formatDate(new Date(), 'yyyy-MM-dd', 'en-US');
  }

  hasAtLeastOneEndedTimeEntry(timeEntries: TimeEntry[]): boolean {
    return timeEntries.some(timeEntry => timeEntry.endTime != null);
  }

  @HostListener("document:keydown.s")
  stopFirstActiveTimeEntry(): void {
    const activeElement: Element | null = document.activeElement;
    if (activeElement instanceof HTMLInputElement) {
      return;
    }
    if (this.activeTimeEntries().length == 0) {
      return;
    }
    this.timeEntryStore.stopActive(this.activeTimeEntries()[0]);
  }

  @HostListener("document:keydown.r")
  resumeLastStoppedTimeEntry(): void {
    const activeElement: Element | null = document.activeElement;
    if (activeElement instanceof HTMLInputElement) {
      return;
    }
    this.timeEntryStore.resumeLastStopped();
  }
}
