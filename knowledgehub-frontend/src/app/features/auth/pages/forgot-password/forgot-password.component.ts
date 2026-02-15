import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { CreateResetRequest } from '../../../../shared/models/auth-service/requests/create-reset-request.model';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authApiService: AuthApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: CreateResetRequest = this.forgotPasswordForm.value;

    this.authApiService.requestPasswordReset(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Password reset link sent to your email';
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 3000);
        } else {
          this.errorMessage = response.error || 'Failed to send reset link';
        }
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.message || 'An error occurred';
        this.loading = false;
      }
    });
  }
}
