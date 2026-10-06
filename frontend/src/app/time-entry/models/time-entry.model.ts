import { Project } from "../../project/models/project.model";
import { Task } from "../../task/models/task.model";

export interface TimeEntry {
  id: number;
  userId: number;
  project: Project;
  task: Task | null;
  description: string | null;
  isBillable: boolean;
  startTime: string;
  endTime: string | null;
  createdAt: string;
}
