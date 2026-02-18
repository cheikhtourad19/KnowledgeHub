import { SubmissionStatus } from '../enums/submission-status.enum';

/**
 * Update PFE Status Request
 */
export interface UpdatePFEStatusRequest {
  status: SubmissionStatus;
  supervisorFeedback?: string;
}
