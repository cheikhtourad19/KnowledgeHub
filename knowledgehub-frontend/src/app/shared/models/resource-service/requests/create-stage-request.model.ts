/**
 * Create Stage Request DTO (for students)
 */
export interface CreateStageRequest {
  title: string;
  companyName: string;
  durationMonths: number;
  conventionFile: globalThis.File;
}
