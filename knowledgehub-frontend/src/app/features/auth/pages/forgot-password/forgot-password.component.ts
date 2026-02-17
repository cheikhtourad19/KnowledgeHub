import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { CreateResetRequest } from '../../../../shared/models/auth-service/requests/create-reset-request.model';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class ForgotPasswordComponent implements OnInit {
  emailForm!: FormGroup;
  codeForm!: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';
  emailSent = false;
  userEmail = '';

  constructor(
    private fb: FormBuilder,
    private authApiService: AuthApiService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Step 1: Email form
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });

    // Step 2: Code verification form
    this.codeForm = this.fb.group({
      code: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i,
          ),
        ],
      ],
    });
  }

  onSubmitEmail(): void {
    if (this.emailForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: CreateResetRequest = this.emailForm.value;

    this.authApiService.requestPasswordReset(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.userEmail = this.emailForm.value.email;
          this.emailSent = true;
          this.successMessage =
            'Code de réinitialisation envoyé! Vérifiez votre email.';
        } else {
          this.errorMessage = response.error || "Échec de l'envoi du code";
        }
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.message || "Une erreur s'est produite";
        this.loading = false;
      },
    });
  }

  onVerifyCode(): void {
    if (this.codeForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const verifyRequest = {
      email: this.userEmail,
      token: this.codeForm.value.code.trim(),
    };

    this.authApiService.verifyResetToken(verifyRequest).subscribe({
      next: (response) => {
        if (response.valid && response.token) {
          this.successMessage = 'Code vérifié! Redirection...';
          setTimeout(() => {
            // Navigate to reset password with token as query param
            this.router.navigate(['/auth/reset-password'], {
              queryParams: { token: response.token },
            });
          }, 1000);
        } else {
          this.errorMessage = response.message || 'Code invalide ou expiré';
        }
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Code invalide ou expiré';
        this.loading = false;
      },
    });
  }

  resendCode(): void {
    this.emailSent = false;
    this.codeForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
  }
}
