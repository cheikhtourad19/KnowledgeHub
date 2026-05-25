import { UserRole } from '../entities/auth-user.model';

/**
 * Auth Response
 * Special response wrapper for auth endpoints with additional fields
 */
export interface AuthResponse<T> {
  success: boolean;
  message: string;
  status: string;
  data?: T;
  error?: string;
  // Auth-specific fields
  token?: string;
  username?: string;
  email?: string;
  role?: UserRole;
  authId?: number;
}
