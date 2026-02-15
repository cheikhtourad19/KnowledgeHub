import {Component, OnInit} from '@angular/core';
import {AuthService} from "./core/services/auth.service";
import {Router} from "@angular/router";
import {UserRole} from "./shared/models/auth-service/entities/auth-user.model";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit{
  title = 'knowledgehub-frontend';

  constructor(private authService: AuthService,private router: Router){

  }

  ngOnInit(): void {
        if (this.authService.isAuthenticated()) {
          this.navigateTo(this.authService.getUserRole())
        }else {
          this.router.navigate(['/auth/login']);
        }
    }
  private navigateTo(role:UserRole | null): void {
      switch (role) {
        case 'STUDENT': this.router.navigate(['student']); break;
        case 'TEACHER': this.router.navigate(['teacher']); break;
        case 'ADMIN': this.router.navigate(['admin']); break;
        default: this.router.navigate(['/auth/login']);
      }
  }



}
