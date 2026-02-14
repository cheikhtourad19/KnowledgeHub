import { Student } from './student.model';

/**
 * Classe Entity
 */
export interface Classe {
  classeId: number;
  classeName: string;
  students?: Student[];
}
