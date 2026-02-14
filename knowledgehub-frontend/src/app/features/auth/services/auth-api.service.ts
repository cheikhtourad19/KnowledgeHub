import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LoginRequest } from '../../../shared/models/auth-service/requests/login-request.model';
import { RegisterRequest } from '../../../shared/models/auth-service/requests/register-request.model';
import { AuthRequest } from '../../../shared/models/auth-service/requests/auth-request.model';
import { CreateResetRequest } from '../../../shared/models/auth-service/requests/create-reset-request.model';
import { ResetRequest } from '../../../shared/models/auth-service/requests/reset-request.model';
import { AuthResponse } from '../../../shared/models/auth-service/responses/auth-response.model';
import { AuthUser } from '../../../shared/models/auth-service/entities/auth-user.model';
import { BulkUserCreationResult } from '../../../shared/models/auth-service/responses/bulk-user-creation-result.model';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly baseUrl = `${environment.apiUrl}/auth-service`;

  constructor(private http: HttpClient) {}

  /**
   * Login user
   */
  login(request: LoginRequest): Observable<AuthResponse<AuthUser>> {
    return this.http.post<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/public/login`,
      request
    );
  }

  /**
   * Register new user (Admin only)
   */
  register(request: RegisterRequest): Observable<AuthResponse<AuthUser>> {
    return this.http.post<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/register`,
      request
    );
  }

  /**
   * Update password
   */
  updatePassword(authId: number, request: AuthRequest): Observable<AuthResponse<AuthUser>> {
    return this.http.put<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/edit-password`,
      request,
      {
        headers: { 'X-Auth-Id': authId.toString() }
      }
    );
  }

  /**
   * Request password reset
   */
  requestPasswordReset(request: CreateResetRequest): Observable<AuthResponse<any>> {
    return this.http.post<AuthResponse<any>>(
      `${this.baseUrl}/api/public/reset-password`,
      request
    );
  }

  /**
   * Reset password with token
   */
  resetPassword(request: ResetRequest): Observable<AuthResponse<any>> {
    return this.http.post<AuthResponse<any>>(
      `${this.baseUrl}/api/public/reset-password/confirm`,
      request
    );
  }

  /**
   * Bulk create users from CSV (Admin only)
   */
  bulkCreateUsers(file: File): Observable<AuthResponse<BulkUserCreationResult>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<AuthResponse<BulkUserCreationResult>>(
      `${this.baseUrl}/api/admin/bulk-create`,
      formData
    );
  }

  /**
   * Get all users (Admin only)
   */
  getAllUsers(): Observable<AuthResponse<AuthUser[]>> {
    return this.http.get<AuthResponse<AuthUser[]>>(
      `${this.baseUrl}/api/admin/users`
    );
  }

  /**
   * Get user by ID (Admin only)
   */
  getUserById(id: number): Observable<AuthResponse<AuthUser>> {
    return this.http.get<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/admin/users/${id}`
    );
  }

  /**
   * Delete user (Admin only)
   */
  deleteUser(id: number): Observable<AuthResponse<any>> {
    return this.http.delete<AuthResponse<any>>(
      `${this.baseUrl}/api/admin/users/${id}`
    );
  }
}
