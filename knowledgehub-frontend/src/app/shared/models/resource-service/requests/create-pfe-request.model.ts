/**
 * Create PFE Request DTO - matches backend CreatePFERequest
 */
export interface CreatePFERequest {
  title: string;
  year?: number;
  studentId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorId: number;
  supervisorName?: string;
  chefDeJuryId?: number;
  chefDeJuryName?: string;
  rapporteurId?: number;
  rapporteurName?: string;
  entrepriseName?: string;
  dateDebut?: Date;
  dateFin?: Date;
}
