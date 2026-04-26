import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {
  step: 'EMAIL' | 'OTP' | 'RESET' = 'EMAIL';
  isSubmitting = false;
  showNewPassword = false;
  showConfirmPassword = false;

  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly emailForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
  });

  readonly otpForm = this.formBuilder.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  });

  readonly resetForm = this.formBuilder.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  toggleNewPassword(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  submitEmail(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const email = this.emailForm.value.email!;
    this.authService
      .forgotPassword(email)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.step = 'OTP';
          this.snackBar.open('OTP sent to your email', 'Close', { duration: 3000 });
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Error sending OTP', 'Close', { duration: 3000 });
        }
      });
  }

  submitOtp(): void {
    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const email = this.emailForm.value.email!;
    const otp = this.otpForm.value.otp!;
    this.authService
      .verifyOtp(email, otp)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.step = 'RESET';
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Invalid OTP', 'Close', { duration: 3000 });
        }
      });
  }

  submitReset(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    const { newPassword, confirmPassword } = this.resetForm.value;
    if (newPassword !== confirmPassword) {
      this.resetForm.get('confirmPassword')?.setErrors({ mismatch: true });
      return;
    }

    this.isSubmitting = true;
    const email = this.emailForm.value.email!;
    const otp = this.otpForm.value.otp!;
    this.authService
      .resetPassword(email, otp, newPassword!)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.snackBar.open('Password reset successfully. You can now login.', 'Close', { duration: 3000 });
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Error resetting password', 'Close', { duration: 3000 });
        }
      });
  }
}
