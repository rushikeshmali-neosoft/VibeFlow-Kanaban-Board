import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  isSubmitting = false;
  showPassword = false;

  private readonly formBuilder = inject(FormBuilder);

  readonly form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService
      .login(this.form.getRawValue() as { email: string; password: string })
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe(() => {
        this.authService.restoreSession().subscribe((user) => {
          if (user && user.role === 'ROLE_ADMIN') {
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.router.navigate(['/board']);
          }
        });
      });
  }
}
