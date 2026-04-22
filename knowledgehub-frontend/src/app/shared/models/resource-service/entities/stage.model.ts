import { File } from './file.model';
import { TypeStage } from '../enums/type-stage.enum';

/**
 * Stage Entity
 */
export interface Stage {
  id: number;
  title: string;
  companyName?: string;
  cloture?: boolean;
  canEdit?: boolean;
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
  conventionFile?: File;
  conventionFileId?: number;
  conventionFileName?: string;
  supervisorFeedback?: string;
  createdDate: Date;
}
