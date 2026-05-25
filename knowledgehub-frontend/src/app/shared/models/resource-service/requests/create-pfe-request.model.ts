/**
 * Create PFE Request DTO - matches backend CreatePFERequest
 */
export interface CreatePFERequest {
  title: string;
  year?: number;
  studentAuthId: number;
  studentName?: string;
  studentEmail?: string;
  supervisorAuthId: number;
  supervisorName?: string;
  chefDeJuryAuthId?: number;
  chefDeJuryName?: string;
  rapporteurAuthId?: number;
  rapporteurName?: string;
  entrepriseName?: string;
  dateDebut?: Date;
  dateFin?: Date;
}
