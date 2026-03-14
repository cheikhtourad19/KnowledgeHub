import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterLink, TooltipModule],
  templateUrl: './topbar.component.html',
})
export class TopbarComponent {
  @Input() homeRoute = '/';
  @Input() currentUser: any = null;
  @Input() userInitials = '';
  @Input() isDarkMode = false;
  @Input() avatarColor: 'dark' | 'red' = 'dark';

  @Output() toggleDark = new EventEmitter<void>();
  @Output() toggleDrawer = new EventEmitter<void>();

  appNamePrefix = environment.app_name_prefix;
  appNameSuffix = environment.app_name_suffix;
}
