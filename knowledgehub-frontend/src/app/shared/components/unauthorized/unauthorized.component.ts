import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../models/auth-service/entities/auth-user.model';

@Component({
  selector: 'app-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.scss'],
  standalone: false,
})
export class UnauthorizedComponent {
  constructor(
    private router: Router,
    private as: AuthService,
  ) {}
  goBack(): void {
    this.router.navigate(['/']);
  }
  goHome(): void {
    if (this.as.isAuthenticated()) {
      if (this.as.hasRole(UserRole.ADMIN)) {
        this.router.navigate(['/admin']);
      } else if (this.as.hasRole(UserRole.TEACHER)) {
        this.router.navigate(['/teacher']);
      } else if (this.as.hasRole(UserRole.STUDENT)) {
        this.router.navigate(['/student']);
      } else {
        this.router.navigate(['/']);
      }
    } else {
      this.router.navigate(['/auth/login']);
    }
  }
}
