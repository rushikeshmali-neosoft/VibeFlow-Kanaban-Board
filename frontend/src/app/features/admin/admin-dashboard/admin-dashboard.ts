import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { UserModel } from '../../../core/models/user.model';
import { TaskModel } from '../../../core/models/task.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit {
  activeTab: 'USERS' | 'TASKS' | 'REPORTS' = 'USERS';
  
  // User Data
  users: UserModel[] = [];
  filteredUsers: UserModel[] = [];
  userSearchQuery = '';

  // Task Data
  tasks: TaskModel[] = [];
  filteredTasks: TaskModel[] = [];
  taskFilterStatus = '';
  taskFilterAssignee = '';

  // Stats
  taskStats: any = null;

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadTasks();
    this.loadStats();
  }

  setTab(tab: 'USERS' | 'TASKS' | 'REPORTS') {
    this.activeTab = tab;
  }

  // --- Users ---
  loadUsers() {
    this.http.get<{data: UserModel[]}>(`${environment.apiBaseUrl}/admin/users`).subscribe(res => {
      this.users = res.data;
      this.filterUsers();
    });
  }

  filterUsers() {
    if (!this.userSearchQuery) {
      this.filteredUsers = this.users;
    } else {
      const q = this.userSearchQuery.toLowerCase();
      this.filteredUsers = this.users.filter(u => u.email.toLowerCase().includes(q));
    }
  }

  toggleUserStatus(userId: number) {
    this.http.post<{data: UserModel}>(`${environment.apiBaseUrl}/admin/users/${userId}/toggle-status`, {}).subscribe(res => {
      const idx = this.users.findIndex(u => u.id === userId);
      if (idx !== -1) {
        this.users[idx] = res.data;
        this.filterUsers();
        this.snackBar.open('User status updated', 'Close', { duration: 2000 });
      }
    });
  }

  // --- Tasks ---
  loadTasks() {
    this.http.get<{data: TaskModel[]}>(`${environment.apiBaseUrl}/admin/tasks`).subscribe(res => {
      this.tasks = res.data;
      this.applyTaskFilters();
    });
  }

  applyTaskFilters() {
    this.filteredTasks = this.tasks.filter(t => {
      const matchStatus = this.taskFilterStatus ? t.status === this.taskFilterStatus : true;
      const assigneeEmail = t.assigneeEmail || '';
      const matchAssignee = this.taskFilterAssignee ? assigneeEmail.toLowerCase().includes(this.taskFilterAssignee.toLowerCase()) : true;
      return matchStatus && matchAssignee;
    });
  }

  deleteTask(taskId: number) {
    if (confirm('Are you sure you want to delete this task?')) {
      this.http.delete(`${environment.apiBaseUrl}/admin/tasks/${taskId}`).subscribe(() => {
        this.tasks = this.tasks.filter(t => t.id !== taskId);
        this.applyTaskFilters();
        this.loadStats();
        this.snackBar.open('Task deleted', 'Close', { duration: 2000 });
      });
    }
  }

  loadStats() {
    this.http.get<{data: any}>(`${environment.apiBaseUrl}/admin/tasks/stats`).subscribe(res => {
      this.taskStats = res.data;
    });
  }

  // --- Reports ---
  downloadReport(type: 'csv' | 'excel' | 'pdf') {
    window.open(`${environment.apiBaseUrl}/admin/export/${type}`, '_blank');
  }
}
