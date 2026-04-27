import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, of, switchMap, take } from 'rxjs';
import { AuthService } from '../services/auth';

export const adminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    switchMap((currentUser) => {
      if (currentUser && currentUser.role === 'ROLE_ADMIN') {
        return of(true);
      } else if (currentUser && currentUser.role !== 'ROLE_ADMIN') {
        router.navigate(['/board']);
        return of(false);
      }

      // Otherwise, restore session from backend to confirm role
      return authService.restoreSession().pipe(
        map((user) => {
          if (user && user.role === 'ROLE_ADMIN') {
            return true;
          }
          router.navigate(['/board']);
          return false;
        }),
        catchError(() => {
          router.navigate(['/login']);
          return of(false);
        })
      );
    })
  );
};
