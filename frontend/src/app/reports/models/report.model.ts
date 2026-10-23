import { Client } from "../../client/models/client.model";
import { Project } from "../../project/models/project.model";

export interface ProjectReport {
  project: Project,
  trackedTimeMillis: number
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
    (projectReport, trackedTimeMillis) => ({ ...projectReport, trackedTimeMillis })
  );
}

export function flattenClientReportEntries(report: Report<ClientReport>): ClientReport[] {
  return flattenReportEntries(
    report,
    clientReport => clientReport.client.name,
    (clientReport, trackedTimeMillis) => ({ ...clientReport, trackedTimeMillis })
  );
}

function flattenReportEntries<T extends { trackedTimeMillis: number }>(
  report: Report<T>,
  getKey: (entry: T) => string,
  mergeEntry: (mergedEntry: T, trackedTimeMillis: number) => T
): T[] {
  const entriesByKey = new Map<string, T>();
  for (const reportDay of report.reportDays) {
    for (const entry of reportDay.reportEntries) {
      const key = getKey(entry);
      const mergedEntry = entriesByKey.get(key);
      entriesByKey.set(
        key,
        mergedEntry
          ? mergeEntry(mergedEntry, mergedEntry.trackedTimeMillis + entry.trackedTimeMillis)
          : entry
      );
    }
  }
  return [...entriesByKey.values()];
}
