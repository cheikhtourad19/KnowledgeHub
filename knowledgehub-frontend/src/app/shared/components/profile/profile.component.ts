import { Component, OnInit } from '@angular/core';
import {
  AuthApiService,
  ResetPasswordRequest,
} from '../../../features/auth/services/auth-api.service';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserApiService } from '../../services/user-api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  public passwordForm: FormGroup;
  public userInitials = '';

  constructor(
    private authService: AuthApiService,
    private auth: AuthService,
    private messageService: MessageService,

    private fb: FormBuilder,
  ) {
    this.passwordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    const user = this.auth.getUser();
    if (user) {
      this.userInitials =
        `${user.prenom?.charAt(0) ?? ''}${user.nom?.charAt(0) ?? ''}`.toUpperCase();
    }
  }

  submitPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { password, confirmPassword, oldPassword } = this.passwordForm.value;

    if (password !== confirmPassword) {
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Les mots de passe ne correspondent pas.',
      });
      return;
    }

    this.editPassword(oldPassword, password);
  }

  private editPassword(oldPassword: string, newPassword: string): void {
    const request: ResetPasswordRequest = { oldPassword, newPassword };

    this.authService.updatePassword(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Mot de passe mis à jour avec succès.',
        });
        this.passwordForm.reset();
      },
      error: (error) => {
        if (error.status === 400) {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Mot de passe incorrect',
          });
          return;
        }
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: error?.message ?? 'Une erreur est survenue.',
        });
      },
    });
  }
}
