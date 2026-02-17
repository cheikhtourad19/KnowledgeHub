import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { ResetRequest } from '../../../../shared/models/auth-service/requests/reset-request.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm!: FormGroup;
  loading = false;
  token = '';

  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authApiService: AuthApiService,
    private router: Router,
    private route: ActivatedRoute,
    private ms: MessageService,
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';

    if (!this.token) {
      this.ms.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Token manquant. Veuillez recommencer.',
      });
    }

    this.resetPasswordForm = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordMatchValidator },
    );
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');

    if (
      password &&
      confirmPassword &&
      password.value !== confirmPassword.value
    ) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || !this.token) {
      return;
    }

    this.loading = true;

    const request: ResetRequest = {
      token: this.token,
      newPassword: this.resetPasswordForm.value.newPassword,
    };

    this.authApiService.resetPassword(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.ms.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Mot de passe réinitialisé avec succès!',
          });
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        } else {
          this.ms.add({
            severity: 'error',
            summary: 'Erreur',
            detail: response.error || "Une erreur s'est produite",
          });
        }
        this.loading = false;
      },
      error: (error) => {
        this.ms.add({
          severity: 'error',
          summary: 'Erreur',
          detail: error.message || "Une erreur s'est produite",
        });
        this.loading = false;
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}
