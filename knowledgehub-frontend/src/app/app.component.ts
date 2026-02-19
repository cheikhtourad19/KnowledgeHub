import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: false,
})
export class AppComponent implements OnInit {
  title = 'knowledgehub-frontend';

  constructor(
    private router: Router,
    private authservice: AuthService,
  ) {}

  ngOnInit(): void {
    const user = this.authservice.getUser();
    if (user) {
      switch (user.role) {
        case 'STUDENT':
          this.router.navigate(['/student']);
          break;
        case 'TEACHER':
          this.router.navigate(['/teacher']);
          break;
        case 'ADMIN':
          this.router.navigate(['/admin']);
          break;
        default:
          this.router.navigate(['/auth/login']);
      }
    }
  }
}
