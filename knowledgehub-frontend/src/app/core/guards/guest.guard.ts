import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../shared/models/auth-service/entities/auth-user.model';

export const guestGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If user is not authenticated, allow access (guest pages)
  if (!authService.isAuthenticated()) {
    return true;
  }

  // User is authenticated - redirect to appropriate dashboard based on role
  const role = authService.getUserRole();

  switch (role) {
    case UserRole.ADMIN:
      router.navigate(['/admin/dashboard']);
      break;
    case UserRole.TEACHER:
      router.navigate(['/teacher/dashboard']);
      break;
    case UserRole.STUDENT:
      router.navigate(['/student/dashboard']);
      break;
    default:
      router.navigate(['/']);
  }

  return false;
};
