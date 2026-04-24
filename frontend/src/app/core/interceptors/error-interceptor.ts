import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = error.error?.message ?? error.error?.error ?? 'Something went wrong';

      if (error.status === 401) {
        authService.logout(false);
        router.navigate(['/login']);
      }

      snackBar.open(message, 'Dismiss', {
        duration: 3500,
        horizontalPosition: 'right',
        verticalPosition: 'top',
      });

      return throwError(() => error);
    }),
  );
};
