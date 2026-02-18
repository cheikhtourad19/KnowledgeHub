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


export interface VerifyResetRequest {
  email: string;
  token: string;
}

export interface VerifyResetResponse {
  valid: boolean;
  token: string | null;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly baseUrl = `${environment.apiUrl}/auth-service`;

  constructor(private http: HttpClient) {}

  
  login(request: LoginRequest): Observable<AuthResponse<AuthUser>> {
    return this.http.post<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/public/login`,
      request,
    );
  }

  
  register(request: RegisterRequest): Observable<AuthResponse<AuthUser>> {
    return this.http.post<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/register`,
      request,
    );
  }

  
  updatePassword(
    authId: number,
    request: AuthRequest,
  ): Observable<AuthResponse<AuthUser>> {
    return this.http.put<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/edit-password`,
      request,
      {
        headers: { 'X-Auth-Id': authId.toString() },
      },
    );
  }

 
  requestPasswordReset(
    request: CreateResetRequest,
  ): Observable<AuthResponse<any>> {
    return this.http.post<AuthResponse<any>>(
      `${this.baseUrl}/api/public/reset-password`,
      request,
    );
  }

  
  verifyResetToken(
    request: VerifyResetRequest,
  ): Observable<VerifyResetResponse> {
    return this.http.post<VerifyResetResponse>(
      `${this.baseUrl}/api/public/verify-reset-token`,
      request,
    );
  }

  
  resetPassword(request: ResetRequest): Observable<AuthResponse<any>> {
    return this.http.post<AuthResponse<any>>(
      `${this.baseUrl}/api/public/reset-password/${request.token}`,
      { newPassword: request.newPassword },
    );
  }

  
  bulkCreateUsers(
    file: File,
  ): Observable<AuthResponse<BulkUserCreationResult>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<AuthResponse<BulkUserCreationResult>>(
      `${this.baseUrl}/api/admin/bulk-create`,
      formData,
    );
  }

  
  getAllUsers(): Observable<AuthResponse<AuthUser[]>> {
    return this.http.get<AuthResponse<AuthUser[]>>(
      `${this.baseUrl}/api/admin/users`,
    );
  }

  
  getUserById(id: number): Observable<AuthResponse<AuthUser>> {
    return this.http.get<AuthResponse<AuthUser>>(
      `${this.baseUrl}/api/admin/users/${id}`,
    );
  }

 
  deleteUser(id: number): Observable<AuthResponse<any>> {
    return this.http.delete<AuthResponse<any>>(
      `${this.baseUrl}/api/admin/users/${id}`,
    );
  }
}
