import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
  standalone: false,
})
export class AdminLayoutComponent implements OnInit {
  isDarkMode = false;
  isDrawerOpen = false;
  currentUser: any = null;
  userInitials = '';
  showLogoutModal = false;

  openDropdowns: { [key: string]: boolean } = {
    'students/gestion': false,
    pfe: false,
  };

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme');
    if (
      savedTheme === 'dark' ||
      (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)
    ) {
      this.isDarkMode = true;
      document.documentElement.classList.add('dark');
    }

    this.loadUserData();
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }

  loadUserData(): void {
    const authState = this.authService.currentAuthState;
    if (authState?.username && authState?.email) {
      this.currentUser = {
        firstName: authState.username.split(/[_\s]/)[0] || authState.username,
        lastName: authState.username.split(/[_\s]/)[1] || '',
        email: authState.email,
        role: authState.role,
      };
      this.userInitials =
        `${this.currentUser.firstName.charAt(0)}${this.currentUser.lastName.charAt(0)}`.toUpperCase();
    }
  }

  toggleDrawer(): void {
    this.isDrawerOpen = !this.isDrawerOpen;
  }
  closeDrawer(): void {
    this.isDrawerOpen = false;
  }
  toggleDropdown(key: string): void {
    this.openDropdowns[key] = !this.openDropdowns[key];
  }
  isDropdownActive(key: string): boolean {
    return this.router.url.includes(`/${key}/`);
  }
  isActive(route: string): boolean {
    return this.router.url.includes(route);
  }

  confirmLogout(): void {
    this.showLogoutModal = false;
    this.authService.clearAuthState();
    this.router.navigate(['/auth/login']);
  }
}
