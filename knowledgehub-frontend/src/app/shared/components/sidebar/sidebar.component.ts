import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input() isOpen = false;
  @Input() currentUser: any = null;
  @Input() userInitials = '';
  @Input() avatarColor: 'dark' | 'red' = 'dark';

  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
}
