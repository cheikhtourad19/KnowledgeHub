import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification-service/entities/notification.model';
import { NotificationRequest } from '../models/notification-service/requests/notification-request.model';
import { NotificationResponse } from '../models/notification-service/responses/notification-response.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationApiService {
  private readonly baseUrl = `${environment.apiUrl}/notification-service`;

  constructor(private http: HttpClient) {}

  /**
   * Send notification (Admin/System)
   */
  sendNotification(request: NotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(
      `${this.baseUrl}/api/notifications/send`,
      request
    );
  }

  /**
   * Get notifications for a user
   */
  getNotificationsByUserId(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/api/notifications/user/${userId}`
    );
  }

  /**
   * Get unread notifications for a user
   */
  getUnreadNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/api/notifications/user/${userId}/unread`
    );
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: string): Observable<Notification> {
    return this.http.put<Notification>(
      `${this.baseUrl}/api/notifications/${notificationId}/read`,
      null
    );
  }

  /**
   * Mark all notifications as read for a user
   */
  markAllAsRead(userId: string): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/api/notifications/user/${userId}/read-all`,
      null
    );
  }

  /**
   * Delete notification
   */
  deleteNotification(notificationId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/notifications/${notificationId}`
    );
  }

  /**
   * Get notification count for user
   */
  getUnreadCount(userId: string): Observable<number> {
    return this.http.get<number>(
      `${this.baseUrl}/api/notifications/user/${userId}/unread-count`
    );
  }
}
