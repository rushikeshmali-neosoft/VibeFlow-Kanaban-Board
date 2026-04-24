import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { UserModel } from '../../core/models/user.model';
import { AuthService } from '../../core/services/auth';
import { TaskDialog } from '../../features/task/task-dialog/task-dialog';
import { TaskModel } from '../../core/models/task.model';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  readonly currentUser$: Observable<UserModel | null>;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
  ) {
    this.currentUser$ = this.authService.currentUser$;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openCreateTask(): void {
    this.dialog
      .open(TaskDialog, {
        width: '42rem',
        data: { mode: 'create' },
      })
      .afterClosed()
      .subscribe((task?: TaskModel) => {
        // Task creation handled by board state via websocket
      });
  }
}
