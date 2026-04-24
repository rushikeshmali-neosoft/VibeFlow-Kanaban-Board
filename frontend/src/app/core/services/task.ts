import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  AssignmentHistoryModel,
  UpdateAssigneeRequest,
} from '../models/assignment-history.model';
import {
  BoardModel,
  CreateTaskRequest,
  ReorderTaskRequest,
  TaskModel,
  UpdateStatusRequest,
} from '../models/task.model';
import { CreateWorklogRequest, WorklogModel } from '../models/worklog.model';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  constructor(private readonly http: HttpClient) {}

  getBoard(): Observable<BoardModel> {
    return this.http
      .get<ApiResponse<BoardModel>>(`${environment.apiBaseUrl}/board`)
      .pipe(map((response) => response.data));
  }

  createTask(payload: CreateTaskRequest): Observable<TaskModel> {
    return this.http
      .post<ApiResponse<TaskModel>>(`${environment.apiBaseUrl}/tasks`, payload)
      .pipe(map((response) => response.data));
  }

  getTask(taskId: number): Observable<TaskModel> {
    return this.http
      .get<ApiResponse<TaskModel>>(`${environment.apiBaseUrl}/tasks/${taskId}`)
      .pipe(map((response) => response.data));
  }

  updateStatus(taskId: number, payload: UpdateStatusRequest): Observable<TaskModel> {
    return this.http
      .patch<ApiResponse<TaskModel>>(`${environment.apiBaseUrl}/tasks/${taskId}/status`, payload)
      .pipe(map((response) => response.data));
  }

  reorder(taskId: number, payload: ReorderTaskRequest): Observable<TaskModel> {
    return this.http
      .patch<ApiResponse<TaskModel>>(`${environment.apiBaseUrl}/tasks/${taskId}/reorder`, payload)
      .pipe(map((response) => response.data));
  }

  updateAssignee(taskId: number, payload: UpdateAssigneeRequest): Observable<TaskModel> {
    return this.http
      .patch<ApiResponse<TaskModel>>(`${environment.apiBaseUrl}/tasks/${taskId}/assignee`, payload)
      .pipe(map((response) => response.data));
  }

  getAssignmentHistory(taskId: number): Observable<AssignmentHistoryModel[]> {
    return this.http
      .get<ApiResponse<AssignmentHistoryModel[]>>(
        `${environment.apiBaseUrl}/tasks/${taskId}/assignment-history`,
      )
      .pipe(map((response) => response.data));
  }

  getWorklogs(taskId: number): Observable<WorklogModel[]> {
    return this.http
      .get<ApiResponse<WorklogModel[]>>(`${environment.apiBaseUrl}/tasks/${taskId}/worklogs`)
      .pipe(map((response) => response.data));
  }

  addWorklog(taskId: number, payload: CreateWorklogRequest): Observable<WorklogModel> {
    return this.http
      .post<ApiResponse<WorklogModel>>(`${environment.apiBaseUrl}/tasks/${taskId}/worklogs`, payload)
      .pipe(map((response) => response.data));
  }
}
