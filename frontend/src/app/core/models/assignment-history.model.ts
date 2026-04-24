export interface AssignmentHistoryModel {
  id: number;
  taskId: number;
  oldAssigneeId?: number | null;
  oldAssigneeEmail?: string | null;
  newAssigneeId?: number | null;
  newAssigneeEmail?: string | null;
  changedById: number;
  changedByEmail: string;
  changedAt: string;
}

export interface UpdateAssigneeRequest {
  assigneeId: number | null;
}
