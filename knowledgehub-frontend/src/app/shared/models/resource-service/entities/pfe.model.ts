import { SubmissionStatus } from '../enums/submission-status.enum';
import { File } from './file.model';

/**
 * PFE Entity - matches backend entity structure
 */
export interface PFE {
  id: number;
  title: string;
  year?: number;
  status: SubmissionStatus;
  cloture?: boolean;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId: number;
  supervisorName?: string;
  chefDeJuryId?: number;
  chefDeJuryName?: string;
  rapporteurId?: number;
  rapporteurName?: string;
  entreprise_name?: string;
  date_debut?: Date;
  date_fin?: Date;
  reportFile?: File;
  attestationFile?: File;
  submissionDate?: Date;
  supervisorFeedback?: string;
  reporteurFeedback?: string;
  plagiarismScore?: number;
  createdDate: Date;
}
