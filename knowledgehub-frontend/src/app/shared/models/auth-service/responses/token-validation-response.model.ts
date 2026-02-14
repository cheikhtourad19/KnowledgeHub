/**
 * Token Validation Response
 */
export interface TokenValidationResponse {
  valid: boolean;
  authId?: number;
  email?: string;
  role?: string;
}
