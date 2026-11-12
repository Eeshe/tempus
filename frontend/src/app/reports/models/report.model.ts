import { Client } from "../../client/models/client.model";
import { Project } from "../../project/models/project.model";

export interface ReportTimeEntry {
  id: number,
  description: string | null,
  isBillable: boolean,
  taskName: string | null,
  startTime: string,
  endTime: string | null,
}

export interface ProjectReport {
  project: Project,
  trackedTimeMillis: number,
  timeEntries: ReportTimeEntry[],
}

export interface ClientReport {
  client: Client,
  trackedTimeMillis: number
}

export interface ReportDay<T> {
  date: string,
  totalTrackedTimeMillis: number,
  totalBillableTrackedTimeMillis: number,
  totalNonBillableTrackedTimeMillis: number,
  totalAccumulatedPay: number,
  reportEntries: T[],
}

export interface Report<T> {
  startDate: string | null,
  endDate: string | null,
  totalTrackedTimeMillis: number,
  totalBillableTrackedTimeMillis: number,
  totalNonBillableTrackedTimeMillis: number,
  totalAccumulatedPay: number,
  reportDays: ReportDay<T>[],
}

export function flattenProjectReportEntries(report: Report<ProjectReport>): ProjectReport[] {
  return flattenReportEntries(
    report,
    projectReport => projectReport.project.name,
    (mergedEntry, entry) => ({
      ...mergedEntry,
      trackedTimeMillis: mergedEntry.trackedTimeMillis + entry.trackedTimeMillis,
      timeEntries: [...mergedEntry.timeEntries, ...entry.timeEntries],
    })
  ).map((projectReport: ProjectReport) => ({
    ...projectReport,
    timeEntries: [...projectReport.timeEntries].sort(compareNewestFirst),
  }));
}

// Show the most recent time entries first, falling back to the highest id so entries
// sharing a start time keep a stable order.
function compareNewestFirst(timeEntryA: ReportTimeEntry, timeEntryB: ReportTimeEntry): number {
  const startTimeDifference: number =
    new Date(timeEntryB.startTime).getTime() - new Date(timeEntryA.startTime).getTime();

  return startTimeDifference !== 0 ? startTimeDifference : timeEntryB.id - timeEntryA.id;
}

export function flattenClientReportEntries(report: Report<ClientReport>): ClientReport[] {
  return flattenReportEntries(
    report,
    clientReport => clientReport.client.name,
    (mergedEntry, entry) => ({
      ...mergedEntry,
      trackedTimeMillis: mergedEntry.trackedTimeMillis + entry.trackedTimeMillis,
    })
  );
}

function flattenReportEntries<T extends { trackedTimeMillis: number }>(
  report: Report<T>,
  getKey: (entry: T) => string,
  mergeEntry: (mergedEntry: T, entry: T) => T
): T[] {
  const entriesByKey = new Map<string, T>();
  for (const reportDay of report.reportDays) {
    for (const entry of reportDay.reportEntries) {
      const key = getKey(entry);
      const mergedEntry = entriesByKey.get(key);
      entriesByKey.set(key, mergedEntry ? mergeEntry(mergedEntry, entry) : entry);
    }
  }
  return [...entriesByKey.values()];
}
