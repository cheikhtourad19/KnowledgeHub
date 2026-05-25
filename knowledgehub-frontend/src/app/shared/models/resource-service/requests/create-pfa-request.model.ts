/**
 * Create PFA Request DTO
 */
export interface CreatePFARequest {
  title: string;
  year?: number;
  studentAuthId: number;
  supervisorAuthId: number;
}
