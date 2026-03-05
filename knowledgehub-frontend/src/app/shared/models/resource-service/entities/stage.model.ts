import { File } from './file.model';
import { TypeStage } from '../enums/type-stage.enum';

/**
 * Stage Entity
 */
export interface Stage {
  id: number;
  title: string;
  companyName?: string;
  typeStage?: TypeStage;
  year?: number;
  durationMonths?: number;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId?: number;
  supervisorName?: string;
  reportFile?: File;
  reportFileId?: number;
  reportFileName?: string;
  journalFile?: File;
  journalFileId?: number;
  journalFileName?: string;
  attestationFile?: File;
  attestationFileId?: number;
  attestationFileName?: string;
  supervisorFeedback?: string;
  createdDate: Date;
}
