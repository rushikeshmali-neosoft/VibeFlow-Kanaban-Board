export type TaskStatus =
  | 'BACKLOG'
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'TESTING'
  | 'DONE'
  | 'CANCELLED'
  | 'CLOSED';

export interface TaskModel {
  id: number;
  title: string;
  status: TaskStatus;
  position: number;
  assigneeId?: number | null;
  assigneeEmail?: string | null;
  createdById?: number | null;
  createdByEmail?: string | null;
  dueDate?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface BoardModel {
  columns: TaskStatus[];
  tasks: TaskModel[];
}

export interface CreateTaskRequest {
  title: string;
  status?: string | null;
  assigneeId?: number | null;
  dueDate?: string | null;
}

export interface UpdateStatusRequest {
  status: TaskStatus;
  position: number;
}

export interface ReorderTaskRequest {
  position: number;
}
