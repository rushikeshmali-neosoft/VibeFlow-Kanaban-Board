import { TaskModel } from './task.model';

export interface WebSocketEventModel {
  type: 'TASK_CREATED' | 'TASK_UPDATED' | 'ASSIGNMENT_UPDATED' | 'WORKLOG_ADDED';
  data: TaskModel;
}
