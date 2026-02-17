/**
 * BulkUserCreationResult
 * Result of bulk user creation from CSV
 */
export interface BulkUserCreationResult {
  email: string;
  nom: string;
  prenom: string;
  notified: boolean;
  reason?: string; // Reason if user wasn't created
}
