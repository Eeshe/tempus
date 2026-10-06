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

export interface Report<T> {
  totalTrackedTimeMillis: number,
  totalBillableTrackedTimeMillis: number,
  totalNonBillableTrackedTimeMillis: number,
  totalAccumulatedPay: number,
  reportEntries: T[],
}
