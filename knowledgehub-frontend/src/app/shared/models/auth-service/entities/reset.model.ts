/**
 * Reset Entity
 * Password reset token information
 */
export interface Reset {
  id: number;
  token: string;
  authUserId: number;
  expiryDate: Date;
  used: boolean;
}
