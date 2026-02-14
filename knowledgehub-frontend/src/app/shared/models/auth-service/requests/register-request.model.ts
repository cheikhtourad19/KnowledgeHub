import { UserRole } from '../entities/auth-user.model';

/**
 * Register Request DTO
 */
export interface RegisterRequest {
  email: string;
  password: string;
  prenom: string;
  nom: string;
  role?: UserRole; // Default: STUDENT
}
