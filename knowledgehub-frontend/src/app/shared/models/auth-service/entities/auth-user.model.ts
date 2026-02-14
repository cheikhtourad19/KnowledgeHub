/**
 * User Role Enum
 */
export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

/**
 * AuthUser Entity
 * Represents authenticated user from auth-service
 */
export interface AuthUser {
  id: number;
  email: string;
  role: UserRole;
  enabled: boolean;
  accountNonLocked: boolean;
  failedLoginAttempts: number;
  lastLogin?: Date;
  passwordChangedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}
