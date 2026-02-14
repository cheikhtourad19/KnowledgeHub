/**
 * Notification Response DTO
 */
export interface NotificationResponse {
  id: string;
  success: boolean;
  message: string;
  delivered: boolean;
  emailSent: boolean;
}
