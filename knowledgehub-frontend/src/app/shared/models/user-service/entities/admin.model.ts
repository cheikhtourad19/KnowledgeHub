import { UserBase } from './user-base.model';

/**
 * Admin Entity
 */
export interface Admin extends UserBase {
  adminId: number;
}
