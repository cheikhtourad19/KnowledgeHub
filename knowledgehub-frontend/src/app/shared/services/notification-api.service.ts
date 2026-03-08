import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification-service/entities/notification.model';
import { NotificationRequest } from '../models/notification-service/requests/notification-request.model';
import { NotificationResponse } from '../models/notification-service/responses/notification-response.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationApiService {
  private readonly baseUrl = `${environment.apiUrl}/notification-service`;

  constructor(private http: HttpClient) {}

  sendNotification(
    request: NotificationRequest,
  ): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(
      `${this.baseUrl}/api/notifications/send`,
      request,
    );
  }

  getNotificationsByUserId(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/api/notifications/user/${userId}`,
    );
  }

  getUnreadNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/api/notifications/user/${userId}/unread`,
    );
  }

  markAsRead(notificationId: string): Observable<Notification> {
    return this.http.put<Notification>(
      `${this.baseUrl}/api/notifications/${notificationId}/read`,
      null,
    );
  }

  markAllAsRead(userId: string): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/api/notifications/user/${userId}/read-all`,
      null,
    );
  }

  deleteNotification(notificationId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/notifications/${notificationId}`,
    );
  }

  getUnreadCount(userId: string): Observable<number> {
    return this.http.get<number>(
      `${this.baseUrl}/api/notifications/user/${userId}/unread-count`,
    );
  }
}
