import { Client } from "../../client/models/client.model";
import { Task } from "../../task/models/task.model";

export interface Project {
  id: number;
  name: string;
  userId: number;
  isPrivate: boolean;
  isArchived: boolean;
  hourlyRate: number;
  tasks: Task[],
  client: Client;
  createdAt: string;
}
