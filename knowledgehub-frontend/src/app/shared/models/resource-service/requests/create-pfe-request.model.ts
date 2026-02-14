/**
 * Create PFE Request DTO
 */
export interface CreatePFERequest {
  title: string;
  year?: number;
  studentId: number;
  supervisorId: number;
  chefDeJuryId?: number;
  rapporteurId?: number;
}
