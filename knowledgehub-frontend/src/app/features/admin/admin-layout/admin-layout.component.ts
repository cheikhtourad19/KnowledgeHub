// admin-layout.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { initFlowbite } from 'flowbite';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent implements OnInit {
  isDrawerOpen = false;
  currentUser: any = null;
  userInitials = '';


  // Admin menu items
  menuItems = [
    {
      label: 'Étudiants',
      icon: 'M12 14l9-5-9-5-9 5 9 5z M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z M12 14l9-5-9-5-9 5 9 5zm0 0l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14zm-4 6v-7.5l4-2.222',
      route: 'etudiants'
    },
    {
      label: 'Enseignants',
      icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z',
      route: 'enseignants'
    },
    {
      label: 'PFE',
      icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      route: 'pfe'
    },
    {
      label: 'PFA',
      icon: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z',
      route: 'pfa'
    },
    {
      label: 'Stage',
      icon: 'M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z',
      route: 'stage'
    },
    {
      label: 'Documents',
      icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      route: 'documents'
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    initFlowbite();
    this.loadUserData();
  }

  loadUserData(): void {
    const authState = this.authService.currentAuthState;

    if (authState && authState.username && authState.email) {
      this.currentUser = {
        firstName: this.extractFirstName(authState.username),
        lastName: this.extractLastName(authState.username),
        email: authState.email,
        role: authState.role
      };

      // Generate initials (First letter of first name + First letter of last name)
      const firstInitial = this.currentUser.firstName.charAt(0).toUpperCase();
      const lastInitial = this.currentUser.lastName.charAt(0).toUpperCase();
      this.userInitials = `${firstInitial}${lastInitial}`;

      // Set user class - for admin, typically null
      // You can customize this logic based on your needs

    }
  }

  extractFirstName(username: string): string {
    // Assuming username format: "firstname_lastname" or "firstname lastname"
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

  logout(): void {
    this.authService.clearAuthState();
    this.router.navigate(['/auth/login']);
  }

  isActive(route: string): boolean {
    return this.router.url.startsWith(route);
  }
}
