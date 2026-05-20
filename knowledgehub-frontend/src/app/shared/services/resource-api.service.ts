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
import { InternshipConventionExtractionResponse } from '../../shared/models/resource-service/responses/internship-convention-extraction-response.model';
import { FileType } from '../../shared/models/resource-service/enums/file-type.enum';
import { map } from 'rxjs/operators';
@Injectable({
  providedIn: 'root',
})
export class ResourceApiService {
  private readonly baseUrl = `${environment.apiUrl}/ressource-service`;
  private readonly conventionExtractionBaseUrl =
    environment.conventionExtractionApiUrl;

  constructor(private http: HttpClient) {}

  uploadFile(
    file: globalThis.File,
    fileType: FileType,
    bucketType: string,
    title: string,
    description?: string,
    coverPage?: globalThis.File,
    isPublished?: boolean,
  ): Observable<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    if (coverPage) {
      formData.append('coverPage', coverPage);
    }
    if (isPublished !== undefined) {
      formData.append('isPublished', String(isPublished));
    }
    formData.append('fileType', fileType as unknown as string);
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

  extractConventionData(
    file: globalThis.File,
  ): Observable<InternshipConventionExtractionResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<InternshipConventionExtractionResponse>(
      `${this.conventionExtractionBaseUrl}/api/internship/extract-convention`,
      formData,
    );
  }

  getFileDownloadUrl(fileId: number): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/api/admin/documents/${fileId}/download`,
      {
        responseType: 'text',
      },
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
      `${this.baseUrl}/api/admin/documents/all`,
    );
  }
  getPublishedDocuments(): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/library/getDocuments`,
    );
  }

  getDocumentsByType(type: FileType): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/library/getDocumentsByType/${type}`,
    );
  }

  searchDocuments(query: string): Observable<ApiResponse<File[]>> {
    return this.http.post<ApiResponse<File[]>>(
      `${this.baseUrl}/api/library/ai-search`,
      { query },
    );
  }

  getFileById(fileId: number): Observable<ApiResponse<File>> {
    return this.http.get<ApiResponse<File>>(
      `${this.baseUrl}/api/library/document/${fileId}`,
    );
  }

  getPresignedUrl(fileId: number): Observable<ApiResponse<string>> {
    return this.http
      .get<
        ApiResponse<string>
      >(`${this.baseUrl}/api/library/document/${fileId}/presigned-url`)
      .pipe(
        map((response) => {
          if (response.data) {
            const presignedUrl = new URL(response.data);
            const publicUrl = new URL(environment.minioPublicUrl);
            presignedUrl.hostname = publicUrl.hostname;
            presignedUrl.port = publicUrl.port;
            presignedUrl.protocol = publicUrl.protocol;
            response.data = presignedUrl.toString();
          }
          console.log('Presigned URL:', response.data);
          return response;
        }),
      );
  }

  getCoverPresignedUrl(fileId: number): Observable<ApiResponse<string>> {
    return this.http
      .get<ApiResponse<string>>(
        `${this.baseUrl}/api/library/document/${fileId}/cover-presigned-url`,
      )
      .pipe(
        map((response) => {
          if (response.data) {
            const presignedUrl = new URL(response.data);
            const publicUrl = new URL(environment.minioPublicUrl);
            presignedUrl.hostname = publicUrl.hostname;
            presignedUrl.port = publicUrl.port;
            presignedUrl.protocol = publicUrl.protocol;
            response.data = presignedUrl.toString();
          }
          return response;
        }),
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
  getMyDemandes(): Observable<ApiResponse<DemandePFE>> {
    return this.http.get<ApiResponse<DemandePFE>>(
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
    teacherId?: number,
  ): Observable<ApiResponse<DemandePFE>> {
    return this.http.put<ApiResponse<DemandePFE>>(
      `${this.baseUrl}/api/admin/pfe/demande/${id}/status?teacherId=${teacherId}`,
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
  getStudentPFE(studentId?: number): Observable<ApiResponse<PFE[]>> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    return this.http.get<ApiResponse<PFE[]>>(
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
  assignJury(
    pfeId: number,
    jury?: number,
    reporteur?: number,
  ): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/pfe/assign-jury/${pfeId}`,
      { juryId: jury, reporteur_id: reporteur },
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
    const formData = new FormData();
    formData.append('title', request.title);
    formData.append('companyName', request.companyName);
    formData.append('durationMonths', request.durationMonths.toString());
    formData.append('typeStage', request.typeStage);
    formData.append('conventionFile', request.conventionFile);

    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage`,
      formData,
    );
  }

  updateStageMetadata(
    stageId: number,
    metadata: {
      title: string;
      companyName: string;
      durationMonths: number;
    },
  ): Observable<ApiResponse<Stage>> {
    return this.http.put<ApiResponse<Stage>>(
      `${this.baseUrl}/api/student/stage/${stageId}/metadata`,
      metadata,
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
      `${this.baseUrl}/api/student/stage/upload-report/${stageId}`,
      formData,
    );
  }

  uploadStageJournal(
    stageId: number,
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
      `${this.baseUrl}/api/student/stage/upload-journal/${stageId}`,
      formData,
    );
  }

  uploadStageAttestation(
    stageId: number,
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
      `${this.baseUrl}/api/student/stage/upload-attestation/${stageId}`,
      formData,
    );
  }

  uploadStageConvention(
    stageId: number,
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
      `${this.baseUrl}/api/student/stage/upload-convention/${stageId}`,
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

  /**
   * Get student's own stage(s)
   * GET /api/student/stage/my-stage
   */
  getMyStage(): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/student/stage/my-stage`,
    );
  }

  /**
   * Check if student has authorization to create a stage
   * GET /api/student/stage/check-authorization
   */
  checkStageAuthorization(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/api/student/stage/check-authorization`,
    );
  }

  viewpdf(fileId: number): Observable<ApiResponse<string>> {
    return this.http.get<ApiResponse<string>>(
      `${this.baseUrl}/api/admin/documents/view/${fileId}`,
    );
  }

  // ========== Stage Authorization Methods (Admin) ==========

  /**
   * Authorize a student to create a stage
   * POST /api/admin/stage/authorize/student/{studentId}?typeStage=TECHNICIEN
   */
  authorizeStudent(
    studentId: number,
    typeStage: string,
  ): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(
      `${this.baseUrl}/api/admin/stage/authorize/student/${studentId}`,
      null,
      { params: { typeStage } },
    );
  }

  /**
   * Authorize all students in a class to create a stage
   * POST /api/admin/stage/authorize/class/{classId}?typeStage=TECHNICIEN
   */
  authorizeClass(
    classId: number,
    typeStage: string,
  ): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(
      `${this.baseUrl}/api/admin/stage/authorize/class/${classId}`,
      null,
      { params: { typeStage } },
    );
  }

  /**
   * Get all stages (Admin)
   * GET /api/admin/stage
   */
  getAllStages(): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/admin/stage`,
    );
  }

  /**
   * Assign a supervisor to a stage (Admin)
   * POST /api/admin/stage/{stageId}/assign-supervisor/{supervisorId}
   */
  assignStageSupervisor(
    stageId: number,
    supervisorId: number,
  ): Observable<ApiResponse<Stage>> {
    return this.http.post<ApiResponse<Stage>>(
      `${this.baseUrl}/api/admin/stage/${stageId}/assign-supervisor/${supervisorId}`,
      null,
    );
  }

  /**
   * Remove all stage creation authorizations (Admin)
   * DELETE /api/admin/stage/remove-all-authorized-students
   */
  removeAllStageAuthorizations(): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(
      `${this.baseUrl}/api/admin/stage/remove-all-authorized-students`,
    );
  }

  closeStageByAdmin(stageId: number): Observable<ApiResponse<Stage>> {
    return this.http.patch<ApiResponse<Stage>>(
      `${this.baseUrl}/api/admin/stage/${stageId}/close`,
      null,
    );
  }

  openStageByAdmin(stageId: number): Observable<ApiResponse<Stage>> {
    return this.http.patch<ApiResponse<Stage>>(
      `${this.baseUrl}/api/admin/stage/${stageId}/open`,
      null,
    );
  }

  setStageCanEditByAdmin(
    stageId: number,
    canEdit: boolean,
  ): Observable<ApiResponse<Stage>> {
    return this.http.patch<ApiResponse<Stage>>(
      `${this.baseUrl}/api/admin/stage/${stageId}/can-edit`,
      null,
      { params: { canEdit: canEdit.toString() } },
    );
  }

  deleteStageByAdmin(stageId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/stage/${stageId}`,
    );
  }

  closeStageByTeacher(stageId: number): Observable<ApiResponse<Stage>> {
    return this.http.patch<ApiResponse<Stage>>(
      `${this.baseUrl}/api/teacher/stage/${stageId}/close`,
      null,
    );
  }

  openStageByTeacher(stageId: number): Observable<ApiResponse<Stage>> {
    return this.http.patch<ApiResponse<Stage>>(
      `${this.baseUrl}/api/teacher/stage/${stageId}/open`,
      null,
    );
  }

  verifyPassword(password: string): Observable<any> {
    const params = new HttpParams().set('password', password);
    return this.http.post(
      `${environment.apiUrl}auth-service/api/verify-password`,
      null,
      { params },
    );
  }

  getMyDocuments(): Observable<ApiResponse<File[]>> {
    return this.http.get<ApiResponse<File[]>>(
      `${this.baseUrl}/api/library/getDocumentsByUser`,
    );
  }

  // Delete every DemandePFE record
  deleteAllDemandes(): Observable<any> {
    return this.http.delete(`${this.baseUrl}/api/admin/pfe/demandes`);
  }

  getTeacherSupervisedPFE(): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/teacher/pfe/supervised`,
    );
  }

  getTeacherJuryPFE(): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/teacher/pfe/jury`,
    );
  }

  getTeacherRepporteurPFE(): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/teacher/pfe/repporteur`,
    );
  }

  updatePFESubmissionStatus(
    pfeId: number,
    request: UpdatePFEStatusRequest,
  ): Observable<ApiResponse<PFE>> {
    return this.http.put<ApiResponse<PFE>>(
      `${this.baseUrl}/api/teacher/pfe/pfe/${pfeId}/status`,
      request,
    );
  }

  submitPFERapporteurFeedback(
    pfeId: number,
    feedback: string,
  ): Observable<ApiResponse<PFE>> {
    return this.http.put<ApiResponse<PFE>>(
      `${this.baseUrl}/api/teacher/pfe/pfe/${pfeId}/rapporteur-feedback`,
      { feedback },
    );
  }

  closePFEByAdmin(pfeId: number): Observable<ApiResponse<PFE>> {
    return this.http.put<ApiResponse<PFE>>(
      `${this.baseUrl}/api/admin/pfe/pfe/${pfeId}/cloture`,
      null,
    );
  }

  cancelPFEClotureByAdmin(pfeId: number): Observable<ApiResponse<PFE>> {
    return this.http.put<ApiResponse<PFE>>(
      `${this.baseUrl}/api/admin/pfe/pfe/${pfeId}/cancel-cloture`,
      null,
    );
  }

  deletePFEByAdmin(pfeId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/pfe/pfe/${pfeId}`,
    );
  }
  deleteDemande(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/admin/pfe/demande/${id}`,
    );
  }
  getTeacherSupervisedStages(): Observable<ApiResponse<Stage[]>> {
    return this.http.get<ApiResponse<Stage[]>>(
      `${this.baseUrl}/api/teacher/stage/supervised`,
    );
  }
  summarizeDocument(
    documentId: number,
  ): Observable<ApiResponse<{ success: boolean; data: string }>> {
    return this.http.post<ApiResponse<{ success: boolean; data: string }>>(
      `${this.baseUrl}/api/library/summarize/${documentId}`,
      {},
    );
  }

  askDocumentQuestion(
    documentId: number,
    question: string,
  ): Observable<ApiResponse<{ success: boolean; data: string }>> {
    return this.http
      .post<
        ApiResponse<{
          file_id: number;
          query: string;
          answer: string;
          source: string;
          characters_in_document: number;
        }>
      >(`${this.baseUrl}/api/library/ask-ai/${documentId}`, { question })
      .pipe(
        map((response) => ({
          ...response,
          data: {
            success: response.success,
            data: response.data?.answer ?? '',
          },
        })),
      );
  }
}
