import { AsyncPipe } from '@angular/common';
import { Component, HostListener, inject, Signal } from '@angular/core';
import { map } from 'rxjs';
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

  readonly timeEntryPage: Signal<TimeEntryPage> = this.timeEntryStore.timeEntryPage;
  readonly activeTimeEntries: Signal<TimeEntry[]> = this.timeEntryStore.activeTimeEntries;
  readonly dayGroupedTimeEntries: Signal<DayGroupedTimeEntries[]> = this.timeEntryStore.dayGroupedTimeEntries;

  readonly todayFormattedTime$ = this.timerService.oneSecondTick$.pipe(map(() => {
    const allTodayTimeEntries: TimeEntry[] = Array.from(this.dayGroupedTimeEntries()[0].allEntries.values()).flat();
    const totalTrackedTimeMs: number = allTodayTimeEntries.reduce((sum, timeEntry) => {
      const trackedTimeMs: number = computeDuration(timeEntry.startTime, timeEntry.endTime!)!.totalMilliseconds;

      return sum + trackedTimeMs;
    }, 0);

    return formatHHMMSSTime(durationFromMs(totalTrackedTimeMs));
  }));

  constructor() {
    super();

    this.timeEntryStore.loadPage();
  }

  override increasePage(): void {
    this.timeEntryStore.loadPage(this.timeEntryPage().nextCursor);
  }

  override decreasePage(): void {
    this.timeEntryStore.loadPage(this.timeEntryPage().previousCursor);
  }

  override updatePage(): void {

  }

  countTotalTimeEntries(map: Map<string, TimeEntry[]>): number {
    return [...map.values()].reduce((sum, arr) => sum + arr.length, 0);
  }

  isTodayGroup(dayGroupedTimeEntries: DayGroupedTimeEntries): boolean {
    const nowDate: Date = new Date();
    const startDate: Date = new Date(dayGroupedTimeEntries.allEntries.values().next().value![0].startTime);

    return nowDate.getDay() == startDate.getDay();
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
}
