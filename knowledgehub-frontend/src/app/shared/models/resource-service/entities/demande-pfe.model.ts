import { DemandStatus } from '../enums/demand-status.enum';
import { File } from './file.model';

/**
 * DemandePFE Entity - matches backend response
 */
export interface DemandePFE {
  id: number;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  studentClasse?: string;
  studentCin: string;
  studentPhone: string;
  pfeTitle: string;
  pfeDescription: string;
  motCles: string;
  entrepriseDomaine: string;
  entrepriseName: string;
  entrepriseEncadrantName: string;
  entrepriseEncadrantEmail: string;
  entrepriseEncadrantPhone: string;
  dateDebut: Date;
  dateFin: Date;
  status: DemandStatus;
  adminFeedback?: string;
  reviewedByAdminId?: number;
  reviewedByAdminName?: string;
  reviewedAt?: Date;
  attestationFile?: File;
  createdAt: Date;
  updatedAt?: Date;
}
