/**
 * Create DemandePFE Request
 */
export interface CreateDemandePFERequest {
  studentCin: string;
  studentPhone: string;
  pfeTitle: string;
  pfeDescription: string;
  motCles: string;
  entrepriseDomaine: string;
  entrepriseName: string;
  entrepriseEncadrantName: string;
  entrepriseEncadrantEmail: string;
  entrepriseEncadrantPhone: string;
  dateDebut: Date;
  dateFin: Date;
}
