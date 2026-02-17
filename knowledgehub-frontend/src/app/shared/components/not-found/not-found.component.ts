// src/app/shared/components/not-found/not-found.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../models/auth-service/entities/auth-user.model';
@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss'],
  standalone: false,
})
export class NotFoundComponent {
  constructor(
    private router: Router,
    private location: Location,
    private as: AuthService,
  ) {}

  goBack(): void {
    this.location.back();
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
