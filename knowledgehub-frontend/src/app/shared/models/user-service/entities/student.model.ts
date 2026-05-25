import { UserBase } from './user-base.model';
import { Classe } from './classe.model';

/**
 * Student Entity
 */
export interface Student extends UserBase {
  authId: string;
  classe?: Classe;
}
