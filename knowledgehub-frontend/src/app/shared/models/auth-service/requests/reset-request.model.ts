/**
 * Reset Request DTO (for password reset with token)
 */
export interface ResetRequest {
  token: string;
  newPassword: string;
}
