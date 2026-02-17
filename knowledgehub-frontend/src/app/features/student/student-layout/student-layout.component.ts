import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-student-layout',
    templateUrl: './student-layout.component.html',
    styleUrls: ['./student-layout.component.scss'],
    standalone: false
})
export class StudentLayoutComponent implements OnInit {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Layout initialization
  }

  logout(): void {
    this.authService.clearAuthState();
    this.router.navigate(['/auth/login']);
  }
}
