import { File } from './file.model';

/**
 * Stage Entity
 */
export interface Stage {
  id: number;
  title: string;
  companyName?: string;
  year?: number;
  durationMonths?: number;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId: number;
  supervisorName?: string;
  reportFile?: File;
  journalFile?: File;
  attestationFile?: File;
  supervisorFeedback?: string;
  createdDate: Date;
}
