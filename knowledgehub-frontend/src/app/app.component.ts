import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { UserRole } from './shared/models/auth-service/entities/auth-user.model';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: false,
})
export class AppComponent implements OnInit {
  title = 'knowledgehub';
  isHomePage = true;

  constructor(
    public authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    // Check if we're on the home page
    this.checkHomePage();

    // Listen to route changes
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.checkHomePage();
      });
  }

  private checkHomePage() {
    this.isHomePage = this.router.url === '/' || this.router.url === '';
  }

  navigateToLogin() {
    this.router.navigate(['/auth/login']);
  }

  navigateToMySpace() {
    const role = this.authService.getUserRole();

    switch (role) {
      case UserRole.STUDENT:
        this.router.navigate(['/student']);
        break;
      case UserRole.TEACHER:
        this.router.navigate(['/teacher']);
        break;
      case UserRole.ADMIN:
        this.router.navigate(['/admin']);
        break;
      default:
        this.router.navigate(['/library']);
    }
  }
}
