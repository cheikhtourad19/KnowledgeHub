import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

@Component({
  selector: 'app-logout-modal',
  standalone: true,
  imports: [DialogModule, ButtonModule],
  templateUrl: './logout-modal.component.html',
})
export class LogoutModalComponent {
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() confirm = new EventEmitter<void>();
}
