import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/common/api-response.model';
import { Student } from '../../shared/models/user-service/entities/student.model';
import { Teacher } from '../../shared/models/user-service/entities/teacher.model';
import { Classe } from '../../shared/models/user-service/entities/classe.model';
import { CreateStudentRequest } from '../../shared/models/user-service/requests/create-student-request.model';
import { StudentToClassRequest } from '../../shared/models/user-service/requests/student-to-class-request.model';
import { BulkUserCreationResponse } from '../../shared/models/user-service/responses/bulk-user-creation-response.model';

@Injectable({
  providedIn: 'root',
})
export class UserApiService {
  bulkAssignStudentsToClass(arg0: {
    studentIds: number[];
    classId: number;
  }): Observable<ApiResponse<Student[]>> {
    return this.http.post<ApiResponse<Student[]>>(
      `${this.baseUrl}/api/admin/add-students-classes`,
      {
        studentId: arg0.studentIds,
        classId: arg0.classId,
      },
    );
  }
  private readonly baseUrl = `${environment.apiUrl}/user-service`;

  constructor(private http: HttpClient) {}

  // ==================== Student Endpoints ====================

  /**
   * Get all students (Admin/Teacher)
   */
  getAllStudents(): Observable<ApiResponse<Student[]>> {
    return this.http.get<ApiResponse<Student[]>>(
      `${this.baseUrl}/api/students/all`,
    );
  }

  /**
   * Get student by ID
   */
  getStudentById(id: number): Observable<ApiResponse<Student>> {
    return this.http.get<ApiResponse<Student>>(
      `${this.baseUrl}/api/students/${id}`,
    );
  }

  /**
   * Bulk create students from CSV (Admin only)
   */
  bulkCreateStudents(
    file: File,
  ): Observable<ApiResponse<BulkUserCreationResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<BulkUserCreationResponse>>(
      `${environment.apiUrl}/auth-service/api/students/upload`,
      formData,
    );
  }

  /**
   * Update student (Admin only)
   */
  updateStudent(
    id: number,
    request: CreateStudentRequest,
  ): Observable<ApiResponse<Student>> {
    return this.http.put<ApiResponse<Student>>(
      `${this.baseUrl}/api/students/${id}`,
      request,
    );
  }

  /**
   * Delete student (Admin only)
   */
  deleteStudent(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/students/${id}`,
    );
  }

  /**
   * Assign student to class (Admin only)
   */
  assignStudentToClass(
    request: StudentToClassRequest,
  ): Observable<ApiResponse<Student>> {
    return this.http.post<ApiResponse<Student>>(
      `${this.baseUrl}/api/admin/add-student-classe`,
      request,
    );
  }

  /**
   * Get students by class ID
   */
  getStudentsByClassId(classId: number): Observable<ApiResponse<Student[]>> {
    return this.http.get<ApiResponse<Student[]>>(
      `${this.baseUrl}/api/classes/${classId}/students`,
    );
  }

  // ==================== Teacher Endpoints ====================

  /**
   * Get all teachers
   */
  getAllTeachers(): Observable<ApiResponse<Teacher[]>> {
    return this.http.get<ApiResponse<Teacher[]>>(
      `${this.baseUrl}/api/teachers`,
    );
  }

  /**
   * Get teacher by ID
   */
  getTeacherById(id: number): Observable<ApiResponse<Teacher>> {
    return this.http.get<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers/${id}`,
    );
  }

  /**
   * Create teacher (Admin only)
   */
  createTeacher(request: any): Observable<ApiResponse<Teacher>> {
    return this.http.post<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers`,
      request,
    );
  }

  /**
   * Update teacher (Admin only)
   */
  updateTeacher(id: number, request: any): Observable<ApiResponse<Teacher>> {
    return this.http.put<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers/${id}`,
      request,
    );
  }

  /**
   * Delete teacher (Admin only)
   */
  deleteTeacher(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/teachers/${id}`,
    );
  }

  // ==================== Class Endpoints ====================

  /**
   * Get all classes
   */
  getAllClasses(): Observable<ApiResponse<Classe[]>> {
    return this.http.get<ApiResponse<Classe[]>>(
      `${this.baseUrl}/api/admin/classe`,
    );
  }

  /**
   * Get class by ID
   */
  getClassById(id: number): Observable<ApiResponse<Classe>> {
    return this.http.get<ApiResponse<Classe>>(
      `${this.baseUrl}/api/classes/${id}`,
    );
  }

  /**
   * Create class (Admin only)
   */
  createClass(className: string): Observable<ApiResponse<Classe>> {
    return this.http.post<ApiResponse<Classe>>(`${this.baseUrl}/api/classes`, {
      classeName: className,
    });
  }

  /**
   * Update class (Admin only)
   */
  updateClass(id: number, className: string): Observable<ApiResponse<Classe>> {
    return this.http.put<ApiResponse<Classe>>(
      `${this.baseUrl}/api/classes/${id}`,
      { classeName: className },
    );
  }

  /**
   * Delete class (Admin only)
   */
  deleteClass(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/classes/${id}`,
    );
  }
}
