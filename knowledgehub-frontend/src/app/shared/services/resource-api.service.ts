import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/common/api-response.model';
import { File } from '../../shared/models/resource-service/entities/file.model';
import { PFE } from '../../shared/models/resource-service/entities/pfe.model';
import { PFA } from '../../shared/models/resource-service/entities/pfa.model';
import { Stage } from '../../shared/models/resource-service/entities/stage.model';
import { CreatePFERequest } from '../../shared/models/resource-service/requests/create-pfe-request.model';
import { CreatePFARequest } from '../../shared/models/resource-service/requests/create-pfa-request.model';
import { CreateStageRequest } from '../../shared/models/resource-service/requests/create-stage-request.model';
import { FileUploadResponse } from '../../shared/models/resource-service/responses/file-upload-response.model';
import { FileType } from '../../shared/models/resource-service/enums/file-type.enum';

@Injectable({
  providedIn: 'root'
})
export class ResourceApiService {
  private readonly baseUrl = `${environment.apiUrl}/ressource-service`;

  constructor(private http: HttpClient) {}

  // ==================== File Management Endpoints ====================

  /**
   * Upload file (Admin/Teacher)
   */
  uploadFile(
    file: globalThis.File,
    fileType: FileType,
    bucketType: string,
    title: string,
    description?: string
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', fileType);
    formData.append('bucketType', bucketType);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/admin/documents/upload`,
      formData
    );
  }

  /**
   * Get presigned download URL
   */
  getFileDownloadUrl(fileId: number): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/api/admin/documents/${fileId}/download`,
      { responseType: 'text' }
    );
  }

  /**
   * Delete file (Admin/Teacher)
   */
  deleteFile(fileId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/documents/${fileId}`
    );
  }

  /**
   * Update file publication status (Admin/Teacher)
   */
  updatePublicationStatus(fileId: number, isPublished: boolean): Observable<ApiResponse<File>> {
    return this.http.put<ApiResponse<File>>(
      `${this.baseUrl}/api/admin/documents/${fileId}/publication`,
      null,
      { params: { isPublished: isPublished.toString() } }
    );
  }

  /**
   * Update file metadata (Admin/Teacher)
   */
  updateFileMetadata(fileId: number, title: string, description: string): Observable<ApiResponse<File>> {
    return this.http.put<ApiResponse<File>>(
      `${this.baseUrl}/api/admin/documents/${fileId}/metadata`,
      { title, description }
    );
  }

  /**
   * Get all documents (Public/Library)
   */
  getAllDocuments(): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/getDocuments`
    );
  }

  /**
   * Get documents by type (Public/Library)
   */
  getDocumentsByType(type: FileType): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/getDocumentsByType/${type}`
    );
  }

  /**
   * Search documents (AI-powered semantic search)
   */
  searchDocuments(query: string): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/searchdocument`,
      { params: { query } }
    );
  }

  // ==================== PFE Endpoints ====================

  /**
   * Create PFE submission (Student)
   */
  createPFE(request: CreatePFERequest): Observable<ApiResponse<PFE>> {
    return this.http.post<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe`,
      request
    );
  }

  /**
   * Get PFE by ID
   */
  getPFEById(id: number): Observable<ApiResponse<PFE>> {
    return this.http.get<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe/${id}`
    );
  }

  /**
   * Get PFEs by student ID
   */
  getPFEsByStudentId(studentId: number): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/student/pfe/student/${studentId}`
    );
  }

  /**
   * Upload PFE report
   */
  uploadPFEReport(pfeId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-report`,
      formData
    );
  }

  /**
   * Upload PFE journal
   */
  uploadPFEJournal(pfeId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-journal`,
      formData
    );
  }

  /**
   * Upload PFE attestation
   */
  uploadPFEAttestation(pfeId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-attestation`,
      formData
    );
  }

  /**
   * Get PFEs by supervisor ID (Teacher)
   */
  getPFEsBySupervisorId(supervisorId: number): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/teacher/pfe/supervisor/${supervisorId}`
    );
  }

  /**
   * Submit PFE feedback (Teacher)
   */
  submitPFEFeedback(pfeId: number, feedback: string): Observable<ApiResponse<PFE>> {
    return this.http.post<ApiResponse<PFE>>(
      `${this.baseUrl}/api/teacher/pfe/${pfeId}/feedback`,
      { feedback }
    );
  }

  // ==================== PFA Endpoints ====================

  /**
   * Create PFA submission (Student)
   */
  createPFA(request: CreatePFARequest): Observable<ApiResponse<PFA>> {
    return this.http.post<ApiResponse<PFA>>(
      `${this.baseUrl}/api/student/pfa`,
      request
    );
  }

  /**
   * Get PFA by ID
   */
  getPFAById(id: number): Observable<ApiResponse<PFA>> {
    return this.http.get<ApiResponse<PFA>>(
      `${this.baseUrl}/api/student/pfa/${id}`
    );
  }

  /**
   * Get PFAs by student ID
   */
  getPFAsByStudentId(studentId: number): Observable<ApiResponse<PFA[]>> {
    return this.http.get<ApiResponse<PFA[]>>(
      `${this.baseUrl}/api/student/pfa/student/${studentId}`
    );
  }

  /**
   * Upload PFA report
   */
  uploadPFAReport(pfaId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfa/${pfaId}/upload-report`,
      formData
    );
  }

  /**
   * Get PFAs by supervisor ID (Teacher)
   */
  getPFAsBySupervisorId(supervisorId: number): Observable<ApiResponse<PFA[]>> {
    return this.http.get<ApiResponse<PFA[]>>(
      `${this.baseUrl}/api/teacher/pfa/supervisor/${supervisorId}`
    );
  }

  /**
   * Submit PFA feedback (Teacher)
   */
  submitPFAFeedback(pfaId: number, feedback: string): Observable<ApiResponse<PFA>> {
    return this.http.post<ApiResponse<PFA>>(
      `${this.baseUrl}/api/teacher/pfa/${pfaId}/feedback`,
      { feedback }
    );
  }

  // ==================== Stage Endpoints ====================

  /**
   * Create Stage submission (Student)
   */
  createStage(request: CreateStageRequest): Observable<ApiResponse<Stage>> {
    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage`,
      request
    );
  }

  /**
   * Get Stage by ID
   */
  getStageById(id: number): Observable<ApiResponse<Stage>> {
    return this.http.get<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage/${id}`
    );
  }

  /**
   * Get Stages by student ID
   */
  getStagesByStudentId(studentId: number): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/student/stage/student/${studentId}`
    );
  }

  /**
   * Upload Stage report
   */
  uploadStageReport(stageId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-report`,
      formData
    );
  }

  /**
   * Upload Stage journal
   */
  uploadStageJournal(stageId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-journal`,
      formData
    );
  }

  /**
   * Upload Stage attestation
   */
  uploadStageAttestation(stageId: number, file: globalThis.File, title: string, description?: string): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-attestation`,
      formData
    );
  }

  /**
   * Get Stages by supervisor ID (Teacher)
   */
  getStagesBySupervisorId(supervisorId: number): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/teacher/stage/supervisor/${supervisorId}`
    );
  }

  /**
   * Submit Stage feedback (Teacher)
   */
  submitStageFeedback(stageId: number, feedback: string): Observable<ApiResponse<Stage>> {
    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/teacher/stage/${stageId}/feedback`,
      { feedback }
    );
  }
}
