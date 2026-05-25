import { NotificationType } from '../enums/notification-type.enum';

/**
 * Notification Entity
 */
export interface Notification {
  id: string;
  authId: string;
  content: string;
  type: NotificationType;
  subject?: string;
  recipientEmail?: string;
  deviceToken?: string;
  delivered: boolean;
  read: boolean;
  createdAt: Date;
  deliveredAt?: Date;
  readAt?: Date;
  emailSent: boolean;
  retryCount: number;
  lastRetryAt?: Date;
}
