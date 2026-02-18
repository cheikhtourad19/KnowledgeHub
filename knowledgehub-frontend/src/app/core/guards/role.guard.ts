import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../shared/models/auth-service/entities/auth-user.model';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  
  if (!authService.isAuthenticated()) {
    router.navigate(['/auth/login']);
    return false;
  }

  
  const requiredRoles = route.data['roles'] as UserRole[];

  
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  
  if (authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  
  router.navigate(['/unauthorized']);
  return false;
};
