/**
 * Create Stage Request DTO
 */
export interface CreateStageRequest {
  title: string;
  companyName?: string;
  year?: number;
  durationMonths?: number;
  studentId: number;
  supervisorId: number;
}
