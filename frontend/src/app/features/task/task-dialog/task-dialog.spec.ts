import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { TaskDialog } from './task-dialog';
import { TaskService } from '../../../core/services/task';
import { BoardStateService } from '../../../core/services/board-state';
import { WebsocketService } from '../../../core/services/websocket';
import { TaskModel } from '../../../core/models/task.model';

describe('TaskDialog (Create Mode) - TDD', () => {
  let component: TaskDialog;
  let fixture: ComponentFixture<TaskDialog>;
  let mockTaskService: jasmine.SpyObj<TaskService>;
  let mockBoardState: jasmine.SpyObj<BoardStateService>;
  let mockWebsocket: jasmine.SpyObj<WebsocketService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<TaskDialog>>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const mockTask: TaskModel = {
    id: 1,
    title: 'Test Task',
    status: 'BACKLOG',
    position: 1,
    createdByEmail: 'test@test.com',
    assigneeEmail: null,
    assigneeId: null,
    dueDate: null,
  };

  beforeEach(async () => {
    mockTaskService = jasmine.createSpyObj('TaskService', ['createTask', 'getTask', 'getWorklogs', 'getAssignmentHistory', 'updateAssignee', 'addWorklog']);
    mockBoardState = jasmine.createSpyObj('BoardStateService', ['upsertTask'], { users$: of([]) });
    mockWebsocket = jasmine.createSpyObj('WebsocketService', [], { events$: of() });
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      declarations: [TaskDialog],
      imports: [
        ReactiveFormsModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: TaskService, useValue: mockTaskService },
        { provide: BoardStateService, useValue: mockBoardState },
        { provide: WebsocketService, useValue: mockWebsocket },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the task dialog in create mode', () => {
    expect(component).toBeTruthy();
    expect(component.data.mode).toBe('create');
  });

  it('should have createForm with required fields', () => {
    expect(component.createForm).toBeDefined();
    expect(component.createForm.get('title')).not.toBeNull();
    expect(component.createForm.get('description')).not.toBeNull();
    expect(component.createForm.get('status')).not.toBeNull();
    expect(component.createForm.get('assigneeId')).not.toBeNull();
  });

  it('should default status to BACKLOG', () => {
    expect(component.createForm.get('status')?.value).toBe('BACKLOG');
  });

  it('should be invalid when title is empty', () => {
    component.createForm.get('title')?.setValue('');
    expect(component.createForm.invalid).toBeTrue();
  });

  it('should be valid when title is provided', () => {
    component.createForm.get('title')?.setValue('My Task');
    expect(component.createForm.valid).toBeTrue();
  });

  it('should not call createTask when form is invalid', () => {
    component.createForm.get('title')?.setValue('');
    component.createTask();
    expect(mockTaskService.createTask).not.toHaveBeenCalled();
  });

  it('should call createTask and close dialog on success', fakeAsync(() => {
    mockTaskService.createTask.and.returnValue(of(mockTask));
    component.createForm.get('title')?.setValue('My New Task');
    component.createTask();
    tick();
    expect(mockTaskService.createTask).toHaveBeenCalled();
    expect(mockBoardState.upsertTask).toHaveBeenCalledWith(mockTask);
    expect(mockDialogRef.close).toHaveBeenCalledWith(mockTask);
  }));

  it('should close dialog without saving when close() is called', () => {
    component.close();
    expect(mockDialogRef.close).toHaveBeenCalled();
  });

  it('should not exceed 255 chars in title', () => {
    component.createForm.get('title')?.setValue('x'.repeat(256));
    expect(component.createForm.get('title')?.hasError('maxlength')).toBeTrue();
  });
});
