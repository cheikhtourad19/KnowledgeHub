// login.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoginRequest } from '../../../../shared/models/auth-service/requests/login-request.model';

import { MessageService } from 'primeng/api';
@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    standalone: false
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private authApiService: AuthApiService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private messageService: MessageService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }


  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const request: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

    this.authApiService.login(request).subscribe({
      next: (response) => {

        this.authService.setAuthState({
          token: response.token ?? null,
          authId: response.authId ?? null,
          userId: response.userId ?? null,
          email: response.email ?? null,
          role: response.role ?? null,
          username: response.username ?? null
        });

        this.isLoading = false;
        console.log('Login successful:', response);
        this.messageService.add({severity:'success', summary:'Success Message', detail:'Login Successful'});
        this.errorMessage=this.authService.getUser()?.role;
        this.navigateByRole(response.role ?? '');
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Login failed. Please try again.';
        this.messageService.add({severity:'error', summary:'Erreur', detail:error.message});

        console.error('Login error:', error);
      }
    });
  }

  private navigateByRole(role: string): void {
    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/admin/']);
        break;
      case 'TEACHER':
        this.router.navigate(['/teacher']);
        break;
      case 'STUDENT':
        this.router.navigate(['/student']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
