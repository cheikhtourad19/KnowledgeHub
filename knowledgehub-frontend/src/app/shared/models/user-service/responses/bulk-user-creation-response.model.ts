import { BulkUserCreationResult } from './bulk-user-creation-result.model';

/**
 * BulkUserCreationResponse
 * Response wrapper for bulk user creation with separated results
 */
export interface BulkUserCreationResponse {
  results: {
    cree: BulkUserCreationResult[];
    non_cree: BulkUserCreationResult[];
  };
}
