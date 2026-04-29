export type InternshipConventionStageType = 'ouvrier' | 'technicien';

export interface InternshipConventionExtractionResponse {
  studentName: string | null;
  studentIdentifiant: string | null;
  studentFiliere: string | null;
  studentCin: string | null;
  studentEmail: string | null;
  studentPhone: string | null;
  studentAddress: string | null;
  entrepriseName: string | null;
  entrepriseDomaine: string | null;
  entrepriseAddress: string | null;
  entreprisePhone: string | null;
  entrepriseEmail: string | null;
  entrepriseSiteWeb: string | null;
  entrepriseEncadrantName: string | null;
  entrepriseEncadrantFonction: string | null;
  entrepriseEncadrantService: string | null;
  entrepriseEncadrantEmail: string | null;
  pfeTitle: string | null;
  dateDebut: string | null;
  dateFin: string | null;
  stageType: InternshipConventionStageType | null;
}
