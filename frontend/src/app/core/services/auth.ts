import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  AuthResponse,
  AuthState,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';
import { UserModel } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly tokenStorageKey = 'vibeflow_token';
  private readonly authStateSubject = new BehaviorSubject<AuthState>({
    token: localStorage.getItem(this.tokenStorageKey),
    currentUser: null,
  });

  readonly authState$ = this.authStateSubject.asObservable();
  readonly currentUser$ = this.authState$.pipe(map((state) => state.currentUser));

  constructor(private readonly http: HttpClient) {}

  restoreSession(): Observable<UserModel | null> {
    if (!this.getToken()) {
      return of(null);
    }

    return this.http
      .get<ApiResponse<UserModel>>(`${environment.apiBaseUrl}/auth/me`)
      .pipe(
        map((response) => response.data),
        tap((user) => this.patchState({ currentUser: user })),
        catchError(() => {
          this.logout(false);
          return of(null);
        }),
      );
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiBaseUrl}/auth/login`, payload)
      .pipe(
        map((response) => response.data),
        tap((response) => this.setToken(response.token)),
      );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiBaseUrl}/auth/register`, payload)
      .pipe(
        map((response) => response.data),
        tap((response) => this.setToken(response.token)),
      );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${environment.apiBaseUrl}/auth/forgot-password`, { email })
      .pipe(map((response) => response.data));
  }

  verifyOtp(email: string, otp: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${environment.apiBaseUrl}/auth/verify-otp`, { email, otp })
      .pipe(map((response) => response.data));
  }

  resetPassword(email: string, otp: string, newPassword: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${environment.apiBaseUrl}/auth/reset-password`, { email, otp, newPassword })
      .pipe(map((response) => response.data));
  }

  logout(callApi = true): void {
    if (callApi && this.getToken()) {
      this.http.post<ApiResponse<void>>(`${environment.apiBaseUrl}/auth/logout`, {}).subscribe({
        next: () => undefined,
        error: () => undefined,
      });
    }

    localStorage.removeItem(this.tokenStorageKey);
    this.authStateSubject.next({ token: null, currentUser: null });
  }

  getToken(): string | null {
    return this.authStateSubject.value.token;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  setCurrentUser(user: UserModel | null): void {
    this.patchState({ currentUser: user });
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
    this.patchState({ token });
  }

  private patchState(partial: Partial<AuthState>): void {
    this.authStateSubject.next({
      ...this.authStateSubject.value,
      ...partial,
    });
  }
}
