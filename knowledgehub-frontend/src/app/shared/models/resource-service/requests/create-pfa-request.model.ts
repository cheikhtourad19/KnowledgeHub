/**
 * Create PFA Request DTO
 */
export interface CreatePFARequest {
  title: string;
  year?: number;
  studentId: number;
  supervisorId: number;
}
