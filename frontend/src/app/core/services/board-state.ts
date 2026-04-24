import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { UserModel } from '../models/user.model';
import { BoardModel, TaskModel, TaskStatus } from '../models/task.model';

const DEFAULT_COLUMNS: TaskStatus[] = [
  'BACKLOG',
  'TODO',
  'IN_PROGRESS',
  'IN_REVIEW',
  'TESTING',
  'DONE',
  'CANCELLED',
  'CLOSED',
];

@Injectable({
  providedIn: 'root',
})
export class BoardStateService {
  private readonly columnsSubject = new BehaviorSubject<TaskStatus[]>(DEFAULT_COLUMNS);
  private readonly tasksSubject = new BehaviorSubject<TaskModel[]>([]);
  private readonly usersSubject = new BehaviorSubject<UserModel[]>([]);

  readonly columns$ = this.columnsSubject.asObservable();
  readonly tasks$ = this.tasksSubject.asObservable();
  readonly users$ = this.usersSubject.asObservable();

  setBoard(board: BoardModel): void {
    const columns = board.columns?.length ? board.columns : DEFAULT_COLUMNS;
    this.columnsSubject.next(columns);
    this.tasksSubject.next(this.sortTasks(board.tasks, columns));
  }

  setUsers(users: UserModel[]): void {
    this.usersSubject.next([...users].sort((left, right) => left.email.localeCompare(right.email)));
  }

  upsertTask(task: TaskModel): void {
    this.upsertTasks([task]);
  }

  upsertTasks(tasks: TaskModel[]): void {
    const nextTasks = this.tasksSubject.value.map((current) => ({ ...current }));
    const indexes = new Map(nextTasks.map((current, index) => [current.id, index]));

    tasks.forEach((task) => {
      const nextTask = { ...task };
      const index = indexes.get(task.id);

      if (index !== undefined) {
        nextTasks[index] = nextTask;
        return;
      }

      indexes.set(task.id, nextTasks.length);
      nextTasks.push(nextTask);
    });

    this.tasksSubject.next(this.sortTasks(nextTasks, this.columnsSubject.value));
  }

  getTask(taskId: number): TaskModel | undefined {
    const task = this.tasksSubject.value.find((current) => current.id === taskId);
    return task ? { ...task } : undefined;
  }

  snapshotTasks(): TaskModel[] {
    return this.tasksSubject.value.map((task) => ({ ...task }));
  }

  tasksForColumn(status: TaskStatus): TaskModel[] {
    return this.tasksSubject.value
      .filter((task) => task.status === status)
      .map((task) => ({ ...task }));
  }

  applyOptimisticMove(taskId: number, targetStatus: TaskStatus, targetPosition: number): TaskModel[] {
    const allTasks = this.tasksSubject.value.map((task) => ({ ...task }));
    const task = allTasks.find((current) => current.id === taskId);

    if (!task) {
      return this.snapshotTasks();
    }

    const sourceTasks = allTasks.filter((current) => current.status === task.status && current.id !== taskId);
    const targetTasks =
      task.status === targetStatus
        ? sourceTasks
        : allTasks.filter((current) => current.status === targetStatus && current.id !== taskId);

    const insertIndex = Math.max(0, Math.min(targetPosition - 1, targetTasks.length));
    const updatedTask = { ...task, status: targetStatus, position: targetPosition };
    const targetTasksWithMove = [...targetTasks];
    targetTasksWithMove.splice(insertIndex, 0, updatedTask);

    const resequencedSourceTasks = this.resequence(sourceTasks);
    const resequencedTargetTasks = this.resequence(targetTasksWithMove);

    const unaffectedTasks = allTasks.filter(
      (current) => current.status !== task.status && current.status !== targetStatus,
    );
    const nextTasks =
      task.status === targetStatus
        ? [...unaffectedTasks, ...resequencedTargetTasks]
        : [...unaffectedTasks, ...resequencedSourceTasks, ...resequencedTargetTasks];

    const sortedTasks = this.sortTasks(nextTasks, this.columnsSubject.value);
    this.tasksSubject.next(sortedTasks);
    return sortedTasks;
  }

  replaceTasks(tasks: TaskModel[]): void {
    const nextTasks = tasks.map((task) => ({ ...task }));
    this.tasksSubject.next(this.sortTasks(nextTasks, this.columnsSubject.value));
  }

  private resequence(tasks: TaskModel[]): TaskModel[] {
    return tasks.map((task, index) => ({
      ...task,
      position: index + 1,
    }));
  }

  private sortTasks(tasks: TaskModel[], columns: TaskStatus[]): TaskModel[] {
    const indexes = new Map(columns.map((column, index) => [column, index]));
    return tasks
      .map((task) => ({ ...task }))
      .sort((left, right) => {
        const leftIndex = indexes.get(left.status) ?? Number.MAX_SAFE_INTEGER;
        const rightIndex = indexes.get(right.status) ?? Number.MAX_SAFE_INTEGER;
        return leftIndex - rightIndex || left.position - right.position;
      });
  }
}
