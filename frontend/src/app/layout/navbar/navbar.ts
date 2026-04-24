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

  /** Returns uppercase initials from an email, e.g. john.doe@... → JD */
  getUserInitials(email: string): string {
    if (!email) return '?';
    const name = email.split('@')[0];
    const parts = name.split(/[._-]/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  /** Returns display name from email, e.g. john.doe@... → John Doe */
  getUserDisplayName(email: string): string {
    if (!email) return 'User';
    const name = email.split('@')[0];
    return name
      .split(/[._-]/)
      .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
      .join(' ');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openCreateTask(): void {
    this.dialog
      .open(TaskDialog, {
        width: '520px',
        maxWidth: '95vw',
        data: { mode: 'create' },
        panelClass: 'vf-dialog',
      })
      .afterClosed()
      .subscribe((task?: TaskModel) => {
        // Task creation handled by board state via websocket
      });
  }
}
