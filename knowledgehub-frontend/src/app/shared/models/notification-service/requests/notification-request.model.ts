import { NotificationType } from '../enums/notification-type.enum';

/**
 * Notification Request DTO
 */
export interface NotificationRequest {
  authId: string;
  content: string;
  type: NotificationType;
  subject?: string;
  recipientEmail?: string;
}
