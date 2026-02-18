import { DemandStatus } from '../enums/demand-status.enum';

/**
 * Update Demand Status Request
 */
export interface UpdateDemandStatusRequest {
  status: DemandStatus;
  adminFeedback?: string;
}
