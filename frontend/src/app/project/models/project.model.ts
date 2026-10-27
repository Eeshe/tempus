import { Client } from "../../client/models/client.model";
import { Task } from "../../task/models/task.model";

export const DEFAULT_PROJECT_COLOR = "#FFFFFF";

export interface Project {
  id: number;
  name: string;
  userId: number;
  isPrivate: boolean;
  isArchived: boolean;
  hourlyRate: number;
  color: string;
  tasks: Task[],
  client: Client;
  createdAt: string;
}
