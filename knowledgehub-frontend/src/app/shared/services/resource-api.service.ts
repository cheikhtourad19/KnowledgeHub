import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/common/api-response.model';
import { File } from '../../shared/models/resource-service/entities/file.model';
import { PFE } from '../../shared/models/resource-service/entities/pfe.model';
import { PFA } from '../../shared/models/resource-service/entities/pfa.model';
import { Stage } from '../../shared/models/resource-service/entities/stage.model';
import { DemandePFE } from '../../shared/models/resource-service/entities/demande-pfe.model';
import { CreatePFERequest } from '../../shared/models/resource-service/requests/create-pfe-request.model';
import { CreateDemandePFERequest } from '../../shared/models/resource-service/requests/create-demande-pfe-request.model';
import { UpdateDemandStatusRequest } from '../../shared/models/resource-service/requests/update-demand-status-request.model';
import { UpdatePFEStatusRequest } from '../../shared/models/resource-service/requests/update-pfe-status-request.model';
import { CreatePFARequest } from '../../shared/models/resource-service/requests/create-pfa-request.model';
import { CreateStageRequest } from '../../shared/models/resource-service/requests/create-stage-request.model';
import { FileUploadResponse } from '../../shared/models/resource-service/responses/file-upload-response.model';
import { FileType } from '../../shared/models/resource-service/enums/file-type.enum';

@Injectable({
  providedIn: 'root',
})
export class ResourceApiService {
  private readonly baseUrl = `${environment.apiUrl}/ressource-service`;

  constructor(private http: HttpClient) {}

  uploadFile(
    file: globalThis.File,
    fileType: FileType,
    bucketType: string,
    title: string,
    description?: string,
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
      formData,
    );
  }

  getFileDownloadUrl(fileId: number): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/api/admin/documents/${fileId}/download`,
      { responseType: 'text' },
    );
  }

  deleteFile(fileId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/documents/${fileId}`,
    );
  }

  updatePublicationStatus(
    fileId: number,
    isPublished: boolean,
  ): Observable<ApiResponse<File>> {
    return this.http.put<ApiResponse<File>>(
      `${this.baseUrl}/api/admin/documents/${fileId}/publication`,
      null,
      { params: { isPublished: isPublished.toString() } },
    );
  }

  updateFileMetadata(
    fileId: number,
    title: string,
    description: string,
  ): Observable<ApiResponse<File>> {
    return this.http.put<ApiResponse<File>>(
      `${this.baseUrl}/api/admin/documents/${fileId}/metadata`,
      { title, description },
    );
  }

  getAllDocuments(): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/getDocuments`,
    );
  }

  getDocumentsByType(type: FileType): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/getDocumentsByType/${type}`,
    );
  }

  searchDocuments(query: string): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/public/library/searchdocument`,
      { params: { query } },
    );
  }

  // ========== PFE Demande Methods ==========

  /**
   * Create a PFE demand with file upload
   * POST /api/student/pfe/demande
   */
  createDemandePFE(
    request: CreateDemandePFERequest,
    file: globalThis.File,
  ): Observable<ApiResponse<DemandePFE>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('studentCin', request.studentCin);
    formData.append('studentPhone', request.studentPhone);
    formData.append('pfeTitle', request.pfeTitle);
    formData.append('pfeDescription', request.pfeDescription);
    formData.append('motCles', request.motCles);
    formData.append('entrepriseDomaine', request.entrepriseDomaine);
    formData.append('entrepriseName', request.entrepriseName);
    formData.append('entrepriseEncadrantName', request.entrepriseEncadrantName);
    formData.append(
      'entrepriseEncadrantEmail',
      request.entrepriseEncadrantEmail,
    );
    formData.append(
      'entrepriseEncadrantPhone',
      request.entrepriseEncadrantPhone,
    );
    formData.append('dateDebut', request.dateDebut.toISOString().split('T')[0]);
    formData.append('dateFin', request.dateFin.toISOString().split('T')[0]);

    return this.http.post<ApiResponse<DemandePFE>>(
      `${this.baseUrl}/api/student/pfe/demande`,
      formData,
    );
  }

  /**
   * Get all demands for the logged-in student
   * GET /api/student/pfe/demandes
   */
  getMyDemandes(): Observable<ApiResponse<DemandePFE[]>> {
    return this.http.get<ApiResponse<DemandePFE[]>>(
      `${this.baseUrl}/api/student/pfe/demandes`,
    );
  }

  /**
   * Get all PFE demands (Admin only)
   * GET /api/admin/pfe/all-demandes
   */
  getAllDemandes(): Observable<ApiResponse<DemandePFE[]>> {
    return this.http.get<ApiResponse<DemandePFE[]>>(
      `${this.baseUrl}/api/admin/pfe/demandes`,
    );
  }

  /**
   * Get demand by ID
   * GET /api/student/pfe/demande/{id}
   */
  getDemandePFEById(id: number): Observable<ApiResponse<DemandePFE>> {
    return this.http.get<ApiResponse<DemandePFE>>(
      `${this.baseUrl}/api/admin/pfe/demande/${id}`,
    );
  }

  /**
   * Update demand status (Admin only)
   * PUT /api/admin/pfe/demande/{id}/status
   */
  updateDemandStatus(
    id: number,
    request: UpdateDemandStatusRequest,
  ): Observable<ApiResponse<DemandePFE>> {
    return this.http.put<ApiResponse<DemandePFE>>(
      `${this.baseUrl}/api/admin/pfe/demande/${id}/status`,
      request,
    );
  }

  // ========== PFE Methods ==========

  /**
   * Create a PFE (Admin only)
   * POST /api/student/pfe/create
   */
  createPFE(request: CreatePFERequest): Observable<ApiResponse<PFE>> {
    return this.http.post<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe/create`,
      request,
    );
  }

  /**
   * Get PFE by ID
   * GET /api/student/pfe/get/{id}
   */
  getPFEById(id: number): Observable<ApiResponse<PFE>> {
    return this.http.get<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe/get/${id}`,
    );
  }

  /**
   * Get PFE for a student
   * GET /api/student/pfe/student-PFE?studentId={studentId}
   */
  getStudentPFE(studentId?: number): Observable<ApiResponse<PFE>> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    return this.http.get<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe/student-PFE`,
      { params },
    );
  }

  /**
   * Get all PFEs supervised by a teacher
   * GET /api/student/pfe/teacher-PFE
   */
  getTeacherPFE(): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/student/pfe/teacher-PFE`,
    );
  }

  /**
   * Get all PFEs (Admin only)
   * GET /api/student/pfe/all-PFE
   */
  getAllPFE(): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/student/pfe/all-PFE`,
    );
  }

  /**
   * Upload PFE report file
   * POST /api/student/pfe/upload-report/{idPFE}
   */
  uploadPFEReport(
    pfeId: number,
    file: globalThis.File,
    title?: string,
    description?: string,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    if (title) {
      formData.append('title', title);
    }
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfe/upload-report/${pfeId}`,
      formData,
    );
  }

  /**
   * Upload PFE attestation file
   * POST /api/student/pfe/upload-attestation/{idPFE}
   */
  uploadPFEAttestation(
    pfeId: number,
    file: globalThis.File,
    title?: string,
    description?: string,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    if (title) {
      formData.append('title', title);
    }
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/pfe/upload-attestation/${pfeId}`,
      formData,
    );
  }

  getPFAsBySupervisorId(supervisorId: number): Observable<ApiResponse<PFA[]>> {
    return this.http.get<ApiResponse<PFA[]>>(
      `${this.baseUrl}/api/teacher/pfa/supervisor/${supervisorId}`,
    );
  }

  submitPFAFeedback(
    pfaId: number,
    feedback: string,
  ): Observable<ApiResponse<PFA>> {
    return this.http.post<ApiResponse<PFA>>(
      `${this.baseUrl}/api/teacher/pfa/${pfaId}/feedback`,
      { feedback },
    );
  }

  createStage(request: CreateStageRequest): Observable<ApiResponse<Stage>> {
    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage`,
      request,
    );
  }

  getStageById(id: number): Observable<ApiResponse<Stage>> {
    return this.http.get<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage/${id}`,
    );
  }

  getStagesByStudentId(studentId: number): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/student/stage/student/${studentId}`,
    );
  }

  uploadStageReport(
    stageId: number,
    file: globalThis.File,
    title: string,
    description?: string,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-report`,
      formData,
    );
  }

  uploadStageJournal(
    stageId: number,
    file: globalThis.File,
    title: string,
    description?: string,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-journal`,
      formData,
    );
  }

  uploadStageAttestation(
    stageId: number,
    file: globalThis.File,
    title: string,
    description?: string,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<ApiResponse<FileUploadResponse>>(
      `${this.baseUrl}/api/student/stage/${stageId}/upload-attestation`,
      formData,
    );
  }

  getStagesBySupervisorId(
    supervisorId: number,
  ): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/teacher/stage/supervisor/${supervisorId}`,
    );
  }

  submitStageFeedback(
    stageId: number,
    feedback: string,
  ): Observable<ApiResponse<Stage>> {
    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/teacher/stage/${stageId}/feedback`,
      { feedback },
    );
  }
}
