/**
 * Create Student Request DTO
 */
export interface CreateStudentRequest {
  authId?: string;
  email: string;
  nom: string;
  prenom: string;
  classeId?: number;
}
