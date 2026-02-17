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

  getAllStudents(): Observable<ApiResponse<Student[]>> {
    return this.http.get<ApiResponse<Student[]>>(
      `${this.baseUrl}/api/students/all`,
    );
  }

  getStudentById(id: number): Observable<ApiResponse<Student>> {
    return this.http.get<ApiResponse<Student>>(
      `${this.baseUrl}/api/students/${id}`,
    );
  }

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

  updateStudent(
    id: number,
    request: CreateStudentRequest,
  ): Observable<ApiResponse<Student>> {
    return this.http.put<ApiResponse<Student>>(
      `${this.baseUrl}/api/students/${id}`,
      request,
    );
  }

  deleteStudent(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/students/${id}`,
    );
  }

  assignStudentToClass(
    request: StudentToClassRequest,
  ): Observable<ApiResponse<Student>> {
    return this.http.post<ApiResponse<Student>>(
      `${this.baseUrl}/api/admin/add-student-classe`,
      request,
    );
  }

  getStudentsByClassId(classId: number): Observable<ApiResponse<Student[]>> {
    return this.http.get<ApiResponse<Student[]>>(
      `${this.baseUrl}/api/classes/${classId}/students`,
    );
  }

  getAllTeachers(): Observable<ApiResponse<Teacher[]>> {
    return this.http.get<ApiResponse<Teacher[]>>(
      `${this.baseUrl}/api/teachers`,
    );
  }

  getTeacherById(id: number): Observable<ApiResponse<Teacher>> {
    return this.http.get<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers/${id}`,
    );
  }

  createTeacher(request: any): Observable<ApiResponse<Teacher>> {
    return this.http.post<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers`,
      request,
    );
  }

  updateTeacher(id: number, request: any): Observable<ApiResponse<Teacher>> {
    return this.http.put<ApiResponse<Teacher>>(
      `${this.baseUrl}/api/teachers/${id}`,
      request,
    );
  }

  deleteTeacher(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/teachers/${id}`,
    );
  }

  getAllClasses(): Observable<ApiResponse<Classe[]>> {
    return this.http.get<ApiResponse<Classe[]>>(
      `${this.baseUrl}/api/admin/classe`,
    );
  }

  getClassById(id: number): Observable<ApiResponse<Classe>> {
    return this.http.get<ApiResponse<Classe>>(
      `${this.baseUrl}/api/classes/${id}`,
    );
  }

  createClass(className: string): Observable<ApiResponse<Classe>> {
    return this.http.post<ApiResponse<Classe>>(`${this.baseUrl}/api/classes`, {
      classeName: className,
    });
  }

  updateClass(id: number, className: string): Observable<ApiResponse<Classe>> {
    return this.http.put<ApiResponse<Classe>>(
      `${this.baseUrl}/api/classes/${id}`,
      { classeName: className },
    );
  }

  deleteClass(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/api/classes/${id}`,
    );
  }
}
