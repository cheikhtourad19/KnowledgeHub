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
  studentAuthId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorAuthId: number;
  supervisorName?: string;
  chefDeJuryAuthId?: number;
  chefDeJuryName?: string;
  rapporteurAuthId?: number;
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
