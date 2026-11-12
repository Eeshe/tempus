import { Client } from '../../client/models/client.model';
import { Project } from '../../project/models/project.model';
import {
  ClientReport,
  flattenClientReportEntries,
  flattenProjectReportEntries,
  ProjectReport,
  Report,
  ReportDay,
  ReportTimeEntry,
} from './report.model';

describe('report.model', () => {
  describe('flattenProjectReportEntries', () => {
    it('merges entries with the same project name and sums their tracked time', () => {
      const project = createProject(1, 'MyProject');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [{ project, trackedTimeMillis: 1_000, timeEntries: [] }]),
        createReportDay('2026-01-02', [{ project, trackedTimeMillis: 2_000, timeEntries: [] }]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        { project, trackedTimeMillis: 3_000, timeEntries: [] },
      ]);
    });

    it('merges the time entries of the same project newest first', () => {
      const project = createProject(1, 'MyProject');
      const firstTimeEntry = createTimeEntry(1, '2026-01-01T09:00:00Z');
      const secondTimeEntry = createTimeEntry(2, '2026-01-02T09:00:00Z');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [
          { project, trackedTimeMillis: 1_000, timeEntries: [firstTimeEntry] },
        ]),
        createReportDay('2026-01-02', [
          { project, trackedTimeMillis: 2_000, timeEntries: [secondTimeEntry] },
        ]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        {
          project,
          trackedTimeMillis: 3_000,
          timeEntries: [secondTimeEntry, firstTimeEntry],
        },
      ]);
    });

    it('orders time entries sharing a start time by descending id', () => {
      const project = createProject(1, 'MyProject');
      const firstTimeEntry = createTimeEntry(1, '2026-01-01T09:00:00Z');
      const secondTimeEntry = createTimeEntry(2, '2026-01-01T09:00:00Z');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [
          {
            project,
            trackedTimeMillis: 2_000,
            timeEntries: [firstTimeEntry, secondTimeEntry],
          },
        ]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        {
          project,
          trackedTimeMillis: 2_000,
          timeEntries: [secondTimeEntry, firstTimeEntry],
        },
      ]);
    });

    it('does not mutate the report entries it receives', () => {
      const project = createProject(1, 'MyProject');
      const firstTimeEntry = createTimeEntry(1, '2026-01-01T09:00:00Z');
      const secondTimeEntry = createTimeEntry(2, '2026-01-01T10:00:00Z');
      const reportEntry: ProjectReport = {
        project,
        trackedTimeMillis: 2_000,
        timeEntries: [firstTimeEntry, secondTimeEntry],
      };
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [reportEntry]),
      ]);

      flattenProjectReportEntries(report);

      expect(reportEntry.timeEntries).toEqual([firstTimeEntry, secondTimeEntry]);
    });

    it('keeps entries with different project names separate', () => {
      const firstProject = createProject(1, 'FirstProject');
      const secondProject = createProject(2, 'SecondProject');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [createProjectReport(firstProject, 1_000)]),
        createReportDay('2026-01-02', [createProjectReport(secondProject, 2_000)]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        createProjectReport(firstProject, 1_000),
        createProjectReport(secondProject, 2_000),
      ]);
    });

    it('returns entries in first-seen order', () => {
      const firstProject = createProject(1, 'FirstProject');
      const secondProject = createProject(2, 'SecondProject');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [createProjectReport(firstProject, 1_000)]),
        createReportDay('2026-01-02', [
          createProjectReport(secondProject, 2_000),
          createProjectReport(firstProject, 3_000),
        ]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        createProjectReport(firstProject, 4_000),
        createProjectReport(secondProject, 2_000),
      ]);
    });

    it('returns an empty list when the report has no days', () => {
      const report = createReport<ProjectReport>([]);

      expect(flattenProjectReportEntries(report)).toEqual([]);
    });
  });

  describe('flattenClientReportEntries', () => {
    it('merges entries with the same client name and sums their tracked time', () => {
      const client = createClient(1, 'MyClient');
      const report = createReport<ClientReport>([
        createReportDay('2026-01-01', [{ client, trackedTimeMillis: 1_000 }]),
        createReportDay('2026-01-02', [{ client, trackedTimeMillis: 2_000 }]),
      ]);

      expect(flattenClientReportEntries(report)).toEqual([
        { client, trackedTimeMillis: 3_000 },
      ]);
    });

    it('keeps entries with different client names separate', () => {
      const firstClient = createClient(1, 'FirstClient');
      const secondClient = createClient(2, 'SecondClient');
      const report = createReport<ClientReport>([
        createReportDay('2026-01-01', [createClientReport(firstClient, 1_000)]),
        createReportDay('2026-01-02', [createClientReport(secondClient, 2_000)]),
      ]);

      expect(flattenClientReportEntries(report)).toEqual([
        createClientReport(firstClient, 1_000),
        createClientReport(secondClient, 2_000),
      ]);
    });

    it('returns an empty list when the report has no days', () => {
      const report = createReport<ClientReport>([]);

      expect(flattenClientReportEntries(report)).toEqual([]);
    });
  });

  function createClientReport(client: Client, trackedTimeMillis: number): ClientReport {
    return { client, trackedTimeMillis };
  }

  function createProjectReport(
    project: Project,
    trackedTimeMillis: number,
    timeEntries: ReportTimeEntry[] = [],
  ): ProjectReport {
    return { project, trackedTimeMillis, timeEntries };
  }

  function createTimeEntry(id: number, startTime: string): ReportTimeEntry {
    return {
      id,
      description: `Description ${id}`,
      isBillable: true,
      taskName: null,
      startTime,
      endTime: startTime,
    };
  }

  function createReportDay<T>(date: string, reportEntries: T[]): ReportDay<T> {
    return {
      date,
      totalTrackedTimeMillis: 0,
      totalBillableTrackedTimeMillis: 0,
      totalNonBillableTrackedTimeMillis: 0,
      totalAccumulatedPay: 0,
      reportEntries,
    };
  }

  function createReport<T>(reportDays: ReportDay<T>[]): Report<T> {
    return {
      startDate: null,
      endDate: null,
      totalTrackedTimeMillis: 0,
      totalBillableTrackedTimeMillis: 0,
      totalNonBillableTrackedTimeMillis: 0,
      totalAccumulatedPay: 0,
      reportDays,
    };
  }

  function createProject(id: number, name: string): Project {
    return {
      id,
      name,
      userId: 1,
      isPrivate: false,
      isArchived: false,
      hourlyRate: 0,
      color: '#FFFFFF',
      tasks: [],
      client: createClient(1, 'MyClient'),
      createdAt: '2026-01-01T00:00:00Z',
    };
  }

  function createClient(id: number, name: string): Client {
    return {
      id,
      name,
      userId: 1,
      createdAt: '2026-01-01T00:00:00Z',
    };
  }
});
