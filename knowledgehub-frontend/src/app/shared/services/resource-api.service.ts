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

  createPFE(request: CreatePFERequest): Observable<ApiResponse<PFE>> {
    return this.http.post<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe`,
      request,
    );
  }

  getPFEById(id: number): Observable<ApiResponse<PFE>> {
    return this.http.get<ApiResponse<PFE>>(
      `${this.baseUrl}/api/student/pfe/${id}`,
    );
  }

  getPFEsByStudentId(studentId: number): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/student/pfe/student/${studentId}`,
    );
  }

  uploadPFEReport(
    pfeId: number,
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
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-report`,
      formData,
    );
  }

  uploadPFEJournal(
    pfeId: number,
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
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-journal`,
      formData,
    );
  }

  uploadPFEAttestation(
    pfeId: number,
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
      `${this.baseUrl}/api/student/pfe/${pfeId}/upload-attestation`,
      formData,
    );
  }

  getPFEsBySupervisorId(supervisorId: number): Observable<ApiResponse<PFE[]>> {
    return this.http.get<ApiResponse<PFE[]>>(
      `${this.baseUrl}/api/teacher/pfe/supervisor/${supervisorId}`,
    );
  }

  submitPFEFeedback(
    pfeId: number,
    feedback: string,
  ): Observable<ApiResponse<PFE>> {
    return this.http.post<ApiResponse<PFE>>(
      `${this.baseUrl}/api/teacher/pfe/${pfeId}/feedback`,
      { feedback },
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
