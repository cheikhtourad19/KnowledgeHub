import { SubmissionStatus } from '../enums/submission-status.enum';
import { File } from './file.model';

/**
 * PFA Entity
 */
export interface PFA {
  id: number;
  title: string;
  year?: number;
  status: SubmissionStatus;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId: number;
  supervisorName?: string;
  reportFile?: File;
  submissionDate?: Date;
  supervisorFeedback?: string;
  createdDate: Date;
  lastModifiedDate: Date;
}
