import { TaskStatus } from './task.model';

export interface TaskTimeReportModel {
  taskId: number;
  title: string;
  status: TaskStatus;
  assignee?: string | null;
  totalHours: number;
}

export interface TimeReportModel {
  tasks: TaskTimeReportModel[];
  grandTotal: number;
}
