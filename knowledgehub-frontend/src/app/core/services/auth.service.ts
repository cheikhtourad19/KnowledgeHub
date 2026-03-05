import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserRole } from '../../shared/models/auth-service/entities/auth-user.model';
import { ApiResponse } from '../../shared/models/common/api-response.model';

export interface AuthState {
  token: string | null;
  authId: number | null;
  userId: number | null;
  email: string | null;
  role: UserRole | null;
  username: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'knowledgehub_token';
  private readonly AUTH_STATE_KEY = 'knowledgehub_auth_state';
  private readonly baseUrl = `${environment.apiUrl}/auth-service/api`;

  private authStateSubject = new BehaviorSubject<AuthState>(this.getStoredAuthState());
  public authState$: Observable<AuthState> = this.authStateSubject.asObservable();

  constructor(private http: HttpClient) {
    // Load auth state on initialization
    this.loadAuthState();
  }

  /**
   * Get current auth state
   */
  get currentAuthState(): AuthState {
    return this.authStateSubject.value;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.currentAuthState.token;
  }

  /**
   * Get current user role
   */
  getUserRole(): UserRole | null {
    return this.currentAuthState.role;
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: UserRole): boolean {
    return this.currentAuthState.role === role;
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: UserRole[]): boolean {
    return roles.includes(this.currentAuthState.role!);
  }

  /**
   * Get stored JWT token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Set authentication state after login
   */
  setAuthState(authState: AuthState): void {
    // Store token separately
    if (authState.token) {
      localStorage.setItem(this.TOKEN_KEY, authState.token);
    }

    // Store auth state
    localStorage.setItem(this.AUTH_STATE_KEY, JSON.stringify(authState));

    // Update subject
    this.authStateSubject.next(authState);
  }

  /**
   * Clear authentication state (logout)
   */
  clearAuthState(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.AUTH_STATE_KEY);

    this.authStateSubject.next({
      token: null,
      authId: null,
      userId: null,
      email: null,
      role: null,
      username: null
    });
  }

  /**
   * Load auth state from storage
   */
  private loadAuthState(): void {
    const storedState = this.getStoredAuthState();
    this.authStateSubject.next(storedState);
  }

  /**
   * Get stored auth state from localStorage
   */
  private getStoredAuthState(): AuthState {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const stateJson = localStorage.getItem(this.AUTH_STATE_KEY);

    if (token && stateJson) {
      try {
        const state = JSON.parse(stateJson);
        return { ...state, token };
      } catch (e) {
        console.error('Failed to parse auth state', e);
      }
    }

    return {
      token: null,
      authId: null,
      userId: null,
      email: null,
      role: null,
      username: null
    };
  }

  /**
   * Get user ID based on role
   */
  getUserId(): number | null {
    return this.currentAuthState.userId;
  }

  getUser():any | null {
    return this.currentAuthState;
  }

  /**
   * Get auth ID
   */
  getAuthId(): number | null {
    return this.currentAuthState.authId;
  }

  /**
   * Verify user password for sensitive operations
   */
  verifyPassword(password: string): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('password', password);
    return this.http.post<ApiResponse<any>>(
      `${this.baseUrl}/verify-password`,
      null,
      { params }
    );
  }
}
