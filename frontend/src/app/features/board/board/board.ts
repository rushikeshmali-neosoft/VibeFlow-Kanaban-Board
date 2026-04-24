import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { BoardStateService } from '../../../core/services/board-state';
import { TaskService } from '../../../core/services/task';
import { UserService } from '../../../core/services/user';
import { TaskModel, TaskStatus } from '../../../core/models/task.model';
import { TaskDialog } from '../../task/task-dialog/task-dialog';

@Component({
  selector: 'app-board',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit {
  columns: TaskStatus[] = [
    'BACKLOG',
    'TODO',
    'IN_PROGRESS',
    'IN_REVIEW',
    'TESTING',
    'DONE',
    'CANCELLED',
    'CLOSED',
  ];
  tasks: TaskModel[] = [];
  columnTaskMap: Record<TaskStatus, TaskModel[]> = this.createEmptyColumnTaskMap();
  /** True only while the initial /board API call is in-flight */
  isLoading = true;

  private readonly destroyRef = inject(DestroyRef);

  readonly columnLabels: Record<TaskStatus, string> = {
    BACKLOG: 'Backlog',
    TODO: 'TODO',
    IN_PROGRESS: 'In Progress',
    IN_REVIEW: 'In Review',
    TESTING: 'Testing',
    DONE: 'Done',
    CANCELLED: 'Cancelled',
    CLOSED: 'Closed',
  };

  readonly columnAccent: Record<TaskStatus, string> = {
    BACKLOG: '#3b82f6',    // Blue
    TODO: '#8b5cf6',       // Purple
    IN_PROGRESS: '#f59e0b', // Amber
    IN_REVIEW: '#10b981',   // Emerald
    TESTING: '#ec4899',     // Pink
    DONE: '#22c55e',        // Green
    CANCELLED: '#ef4444',   // Red
    CLOSED: '#6b7280',      // Gray
  };

  constructor(
    private readonly boardStateService: BoardStateService,
    private readonly taskService: TaskService,
    private readonly userService: UserService,
    private readonly dialog: MatDialog,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.boardStateService.columns$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((columns) => {
        this.columns = columns;
        this.rebuildTasksByColumn(this.tasks);
        this.cdr.markForCheck();
      });

    this.boardStateService.tasks$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((tasks) => {
        this.tasks = tasks;
        this.rebuildTasksByColumn(tasks);
        this.cdr.markForCheck();
      });

    // Kick off the API fetch — isLoading is cleared in loadBoard()
    this.loadBoard();
  }

  openCreateTask(): void {
    this.dialog
      .open(TaskDialog, {
        width: '42rem',
        data: { mode: 'create' },
        backdropClass: 'vf-dialog-backdrop',
        panelClass: 'vf-dialog-panel',
      })
      .afterClosed()
      .subscribe((task?: TaskModel) => {
        if (task) {
          this.boardStateService.upsertTask(task);
        }
      });
  }

  openTask(task: TaskModel): void {
    this.dialog.open(TaskDialog, {
      width: '48rem',
      data: { taskId: task.id, mode: 'detail' },
      backdropClass: 'vf-dialog-backdrop',
      panelClass: 'vf-dialog-panel',
    });
  }

  drop(event: CdkDragDrop<TaskModel[]>, targetStatus: TaskStatus): void {
    const task = event.item.data;
    const nextPosition = event.currentIndex + 1;
    const previousTasks = this.boardStateService.snapshotTasks().map((current) => ({ ...current }));

    this.boardStateService.applyOptimisticMove(task.id, targetStatus, nextPosition);

    const request$ =
      task.status === targetStatus
        ? this.taskService.reorder(task.id, { position: nextPosition })
        : this.taskService.updateStatus(task.id, { status: targetStatus, position: nextPosition });

    request$.subscribe({
      next: (updatedTask) => this.boardStateService.upsertTask(updatedTask),
      error: () => this.boardStateService.replaceTasks(previousTasks),
    });
  }

  trackByTask(_: number, task: TaskModel): number {
    return task.id;
  }

  trackByColumn(_: number, column: TaskStatus): TaskStatus {
    return column;
  }

  private loadBoard(): void {
    this.isLoading = true;
    this.taskService.getBoard().subscribe({
      next: (board) => {
        this.boardStateService.setBoard(board);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });

    this.userService.getUsers().subscribe({
      next: (users) => this.boardStateService.setUsers(users),
      error: () => undefined,
    });
  }

  private rebuildTasksByColumn(tasks: TaskModel[]): void {
    const next = this.createEmptyColumnTaskMap();

    tasks.forEach((task) => {
      next[task.status].push({ ...task });
    });

    (Object.keys(next) as TaskStatus[]).forEach((status) => {
      next[status].sort((left, right) => left.position - right.position);
    });

    this.columnTaskMap = next;
  }

  private createEmptyColumnTaskMap(): Record<TaskStatus, TaskModel[]> {
    return {
      BACKLOG: [],
      TODO: [],
      IN_PROGRESS: [],
      IN_REVIEW: [],
      TESTING: [],
      DONE: [],
      CANCELLED: [],
      CLOSED: [],
    };
  }
}
