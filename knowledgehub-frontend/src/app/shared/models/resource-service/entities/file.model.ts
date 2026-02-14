import { FileType } from '../enums/file-type.enum';

/**
 * File Entity
 */
export interface File {
  id: number;
  fileName: string;
  originalFileName: string;
  title: string;
  description?: string;
  isPublished: boolean;
  fileSize: number;
  storagePath: string;
  uploadDate: Date;
  uploadedByUserId: number;
  uploadedByUserName?: string;
  pageCount?: number;
  fileType: FileType;
  isScanned: boolean;
}
