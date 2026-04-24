import { Component, DestroyRef, Inject, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { filter, forkJoin } from 'rxjs';
import { AssignmentHistoryModel } from '../../../core/models/assignment-history.model';
import { TaskModel } from '../../../core/models/task.model';
import { UserModel } from '../../../core/models/user.model';
import { WorklogModel } from '../../../core/models/worklog.model';
import { BoardStateService } from '../../../core/services/board-state';
import { TaskService } from '../../../core/services/task';
import { WebsocketService } from '../../../core/services/websocket';

interface TaskDialogData {
  mode: 'create' | 'detail';
  taskId?: number;
}

@Component({
  selector: 'app-task-dialog',
  standalone: false,
  templateUrl: './task-dialog.html',
  styleUrl: './task-dialog.scss',
})
export class TaskDialog implements OnInit {
  task?: TaskModel;
  users: UserModel[] = [];
  worklogs: WorklogModel[] = [];
  assignmentHistory: AssignmentHistoryModel[] = [];
  isBusy = false;
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);

  readonly createForm = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(1000)]],
    status: ['BACKLOG'],
    assigneeId: [null as number | null],
    dueDate: [null as Date | null],
  });

  readonly assigneeForm = this.formBuilder.group({
    assigneeId: [null as number | null],
  });

  readonly worklogForm = this.formBuilder.group({
    hours: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) readonly data: TaskDialogData,
    private readonly dialogRef: MatDialogRef<TaskDialog>,
    private readonly taskService: TaskService,
    private readonly boardStateService: BoardStateService,
    private readonly websocketService: WebsocketService,
    private readonly snackBar: MatSnackBar,
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.boardStateService.users$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((users) => (this.users = users));

    if (this.data.mode === 'detail' && this.data.taskId) {
      this.loadDetail(this.data.taskId);
      this.websocketService.events$
        .pipe(
          takeUntilDestroyed(this.destroyRef),
          filter((event) => event.data.id === this.data.taskId),
        )
        .subscribe(() => this.loadDetail(this.data.taskId!));
    }
  }

  createTask(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isBusy = true;
    const formValue = this.createForm.getRawValue();

    this.taskService
      .createTask({
        title: formValue.title ?? '',
        dueDate: this.toDateOnly(formValue.dueDate),
        // ── ROOT CAUSE FIX: pass assigneeId so the backend sets the assignee
        // immediately; the returned TaskDTO will already contain assigneeEmail
        assigneeId: formValue.assigneeId ?? null,
      })
      .subscribe({
        next: (task) => {
          // upsertTask updates board state with the full task (assigneeEmail included)
          this.boardStateService.upsertTask(task);
          this.snackBar.open('Task created successfully', 'Dismiss', { duration: 2500 });
          this.closeWith(task);
        },
        error: () => {
          this.isBusy = false;
        },
      });
  }

  saveAssignee(): void {
    if (!this.task) {
      return;
    }

    this.taskService
      .updateAssignee(this.task.id, {
        assigneeId: this.assigneeForm.getRawValue().assigneeId ?? null,
      })
      .subscribe((updatedTask) => {
        this.task = updatedTask;
        this.boardStateService.upsertTask(updatedTask);
        this.snackBar.open('Assignee updated successfully', 'Dismiss', { duration: 2500 });
        this.loadDetail(updatedTask.id);
      });
  }

  addWorklog(): void {
    if (!this.task || this.worklogForm.invalid) {
      this.worklogForm.markAllAsTouched();
      return;
    }

    this.taskService
      .addWorklog(this.task.id, {
        hours: Number(this.worklogForm.getRawValue().hours),
      })
      .subscribe(() => {
        this.worklogForm.reset();
        this.snackBar.open('Worklog added successfully', 'Dismiss', { duration: 2500 });
        this.loadDetail(this.task!.id);
      });
  }

  private loadDetail(taskId: number): void {
    this.isBusy = true;
    forkJoin({
      task: this.taskService.getTask(taskId),
      worklogs: this.taskService.getWorklogs(taskId),
      history: this.taskService.getAssignmentHistory(taskId),
    }).subscribe({
      next: ({ task, worklogs, history }) => {
        this.task = task;
        this.worklogs = worklogs;
        this.assignmentHistory = history;
        this.assigneeForm.patchValue({
          assigneeId: task.assigneeId ?? null,
        });
        this.isBusy = false;
      },
      error: () => {
        this.isBusy = false;
      },
    });
  }

  private toDateOnly(date: Date | null): string | null {
    if (!date) {
      return null;
    }

    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private closeWith(task: TaskModel): void {
    this.dialogRef.close(task);
  }
}
