import { SubmissionStatus } from '../enums/submission-status.enum';
import { File } from './file.model';

/**
 * PFE Entity
 */
export interface PFE {
  id: number;
  title: string;
  year?: number;
  status: SubmissionStatus;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId: number;
  supervisorName?: string;
  chefDeJuryId?: number;
  chefDeJuryName?: string;
  rapporteurId?: number;
  rapporteurName?: string;
  reportFile?: File;
  journalFile?: File;
  attestationFile?: File;
  submissionDate?: Date;
  supervisorFeedback?: string;
  plagiarismScore?: number;
  createdDate: Date;
}
