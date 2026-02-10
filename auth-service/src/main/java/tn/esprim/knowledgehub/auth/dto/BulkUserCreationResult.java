package tn.esprim.knowledgehub.auth.dto;

import com.knowledgehub.notification.client.dto.NotificationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.auth.entity.AuthUser;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserCreationResult {
    private AuthUser user;
    private NotificationResponse notification;
}
