/**
 * File Download Response DTO
 */
export interface FileDownloadResponse {
  fileId: number;
  fileName: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  downloadUrl: string;
  expiresIn: number;
}
