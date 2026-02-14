/**
 * Generic API Response wrapper
 * Used by User Service and Resource Service
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}
