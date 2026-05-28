export interface TeacherCVResponse {
  id: number;
  teacherId: number;
  teacherName: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  extractedText: string;
  aiSummary: string;
  uploadedAt: string;
  active: boolean;
  version: number;
}
