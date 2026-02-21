// admin-layout.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { initFlowbite } from 'flowbite';

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
    etudiants: false,
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
    initFlowbite();
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

    if (authState && authState.username && authState.email) {
      this.currentUser = {
        firstName: this.extractFirstName(authState.username),
        lastName: this.extractLastName(authState.username),
        email: authState.email,
        role: authState.role,
      };

      const firstInitial = this.currentUser.firstName.charAt(0).toUpperCase();
      const lastInitial = this.currentUser.lastName.charAt(0).toUpperCase();
      this.userInitials = `${firstInitial}${lastInitial}`;
    }
  }

  extractFirstName(username: string): string {
    const parts = username.split(/[_\s]/);
    return parts[0] || username;
  }

  extractLastName(username: string): string {
    const parts = username.split(/[_\s]/);
    return parts[1] || '';
  }

  toggleDrawer(): void {
    this.isDrawerOpen = !this.isDrawerOpen;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  toggleDropdown(dropdown: string): void {
    this.openDropdowns[dropdown] = !this.openDropdowns[dropdown];
  }

  isDropdownActive(dropdown: string): boolean {
    return this.router.url.includes(`/${dropdown}/`);
  }

  confirmLogout(): void {
    this.showLogoutModal = false;
    this.authService.clearAuthState();
    this.router.navigate(['/auth/login']);
  }

  isActive(route: string): boolean {
    return this.router.url.includes(route);
  }
}
