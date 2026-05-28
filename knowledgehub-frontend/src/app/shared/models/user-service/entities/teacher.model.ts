import { UserBase } from './user-base.model';

/**
 * Teacher Entity
 */
export interface Teacher extends UserBase {
  authId: string;
  teacherId?: number;
}
