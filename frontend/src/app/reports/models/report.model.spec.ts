import { Client } from '../../client/models/client.model';
import { Project } from '../../project/models/project.model';
import {
  ClientReport,
  flattenClientReportEntries,
  flattenProjectReportEntries,
  ProjectReport,
  Report,
  ReportDay,
} from './report.model';

describe('report.model', () => {
  describe('flattenProjectReportEntries', () => {
    it('merges entries with the same project name and sums their tracked time', () => {
      const project = createProject(1, 'MyProject');
      const report = createReport<ProjectReport>([
        createReportDay('2026-01-01', [{ project, trackedTimeMillis: 1_000 }]),
        createReportDay('2026-01-02', [{ project, trackedTimeMillis: 2_000 }]),
      ]);

      expect(flattenProjectReportEntries(report)).toEqual([
        { project, trackedTimeMillis: 3_000 },
      ]);
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

  function createProjectReport(project: Project, trackedTimeMillis: number): ProjectReport {
    return { project, trackedTimeMillis };
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
