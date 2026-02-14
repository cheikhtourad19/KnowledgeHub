// error.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { ApiResponse } from '../../shared/models/common/api-response.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unknown error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error (network issue, etc.)
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error - extract from ApiResponse structure
        const apiResponse = error.error as ApiResponse<any>;

        // Handle specific HTTP status codes
        if (error.status === 401) {
          // Unauthorized - clear auth and redirect to login
          authService.clearAuthState();
          router.navigate(['/auth/login']);
          errorMessage = apiResponse?.message || apiResponse?.error || 'Session expired. Please login again.';
        } else if (error.status === 403) {
          errorMessage = apiResponse?.message || apiResponse?.error || 'Access forbidden. You do not have permission.';
        } else if (error.status === 404) {
          errorMessage = apiResponse?.message || apiResponse?.error || 'Resource not found.';
        } else if (error.status === 500) {
          errorMessage = apiResponse?.message || apiResponse?.error || 'Internal server error. Please try again later.';
        } else if (error.status === 400) {
          errorMessage = apiResponse?.message || apiResponse?.error || 'Bad request. Please check your input.';
        } else if (error.status === 0) {
          // Network error or CORS issue
          errorMessage = 'Unable to connect to server. Please check your internet connection.';
        } else {
          // Generic error handling - try to extract message from ApiResponse
          errorMessage = apiResponse?.message ||
            apiResponse?.error ||
            error.message ||
            `Error Code: ${error.status}`;
        }
      }

      console.error('HTTP Error:', {
        status: error.status,
        message: errorMessage,
        url: error.url,
        fullError: error
      });

      return throwError(() => new Error(errorMessage));
    })
  );
};
