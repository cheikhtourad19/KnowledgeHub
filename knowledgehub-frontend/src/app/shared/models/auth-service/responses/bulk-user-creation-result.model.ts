/**
 * Bulk User Creation Result
 */
export interface BulkUserCreationResult {
  totalProcessed: number;
  successCount: number;
  failureCount: number;
  errors: string[];
  createdUsers: Array<{
    email: string;
    role: string;
    authId: number;
  }>;
}
