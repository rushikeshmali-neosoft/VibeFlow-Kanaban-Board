import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Board } from './features/board/board/board';
import { TimeReport } from './features/report/time-report/time-report';

import { ForgotPassword } from './features/auth/forgot-password/forgot-password';
import { AdminDashboard } from './features/admin/admin-dashboard/admin-dashboard';
import { adminGuard } from './core/guards/admin-guard';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'board' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'admin/dashboard', component: AdminDashboard, canActivate: [adminGuard] },
  { path: 'board', component: Board, canActivate: [authGuard] },
  { path: 'reports/time', component: TimeReport, canActivate: [authGuard] },
  { path: '**', redirectTo: 'board' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
