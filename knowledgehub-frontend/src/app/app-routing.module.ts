import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { UserRole } from './shared/models/auth-service/entities/auth-user.model';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';
import { NotFoundComponent } from './shared/components/not-found/not-found.component';
import { AppComponent } from './app.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full',
  },

  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),
  },

  {
    path: 'student',
    loadChildren: () =>
      import('./features/student/student.module').then((m) => m.StudentModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: [UserRole.STUDENT, UserRole.ADMIN] },
  },

  {
    path: 'teacher',
    loadChildren: () =>
      import('./features/teacher/teacher.module').then((m) => m.TeacherModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: [UserRole.TEACHER, UserRole.ADMIN] },
  },

  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.module').then((m) => m.AdminModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: [UserRole.ADMIN] },
  },

  {
    path: 'library',
    loadChildren: () =>
      import('./features/library/library.module').then((m) => m.LibraryModule),
    canActivate: [authGuard],
  },

  {
    path: 'notifications',
    loadChildren: () =>
      import('./features/notification/notification.module').then(
        (m) => m.NotificationModule,
      ),
    canActivate: [authGuard],
  },

  {
    path: 'unauthorized',
    component: UnauthorizedComponent,
  },

  {
    path: '**',
    component: NotFoundComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
