import { FileType } from '../enums/file-type.enum';

/**
 * File Upload Response DTO
 */
export interface FileUploadResponse {
  fileId: number;
  fileName: string;
  originalFileName: string;
  title: string;
  description?: string;
  fileSize: number;
  contentType: string;
  storagePath: string;
  fileType: FileType;
  uploadDate: Date;
  uploadedByUserId: number;
  uploadedByUserName?: string;
  pageCount?: number;
  isPublished: boolean;
  message: string;
}
