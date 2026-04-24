export interface WorklogModel {
  id: number;
  taskId: number;
  userId: number;
  userEmail: string;
  hours: number;
  createdAt: string;
}

export interface CreateWorklogRequest {
  hours: number;
}
